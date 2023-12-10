package com.jeontongju.auction.service;

import com.jeontongju.auction.client.ConsumerServiceFeignClient;
import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.kafka.KafkaAuctionBidHistoryDto;
import com.jeontongju.auction.dto.kafka.KafkaChatMessageDto;
import com.jeontongju.auction.dto.redis.AuctionBidHistoryDto;
import com.jeontongju.auction.dto.redis.MemberDto;
import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import com.jeontongju.auction.dto.response.AuctionBroadcastResponseDto;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionInvalidStatusException;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.exception.ConsumerInvalidCreditException;
import com.jeontongju.auction.exception.SameBidPriceException;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoHistoryRepository;
import com.jeontongju.auction.vo.BidInfoHistoryId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastingService {

  private final AuctionRepository auctionRepository;
  private final BidInfoHistoryRepository bidInfoHistoryRepository;

  private final RedisTemplate redisTemplate;

  private final SimpMessagingTemplate template;
  private final KafkaTemplate<String, KafkaChatMessageDto> kafkaChatTemplate;
  private final KafkaTemplate<String, String> kafkaBidInfoTemplate;

  private static final String CHAT_TOPIC = "bid-chat-topic";
  private static final String BID_FIN_TOPIC = "bid-info-topic";

  private final ConsumerServiceFeignClient client;

  public void startAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.ING)) {
      throw new AuctionInvalidStatusException("이미 진행 중인 경매입니다.");
    } else if (status.equals(AuctionStatusEnum.AFTER)) {
      throw new AuctionInvalidStatusException("이미 완료된 경매입니다.");
    }

    auctionRepository.save(auction.toBuilder().status(AuctionStatusEnum.ING).build());
  }

  public void endAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.BEFORE)) {
      throw new AuctionInvalidStatusException("경매가 시작하지 않았습니다.");
    } else if (status.equals(AuctionStatusEnum.AFTER)) {
      throw new AuctionInvalidStatusException("이미 완료된 경매입니다.");
    }

    auctionRepository.save(auction.toBuilder().status(AuctionStatusEnum.AFTER).build());
  }

  public void bidProduct(AuctionBidRequestDto auctionBidRequestDto, Long consumerId) {
    // 1. 크레딧 검사
    ValueOperations<String, MemberDto> memberRedis = redisTemplate.opsForValue();
    MemberDto memberDto = memberRedis.get(consumerId);
    Long memberCredit = memberDto.getCredit();

    if (memberCredit == null || memberCredit < auctionBidRequestDto.getBidPrice()) {
      throw new ConsumerInvalidCreditException();
    }

    // 2. 동일 상품 + 동일 입찰가 데이터 검사
    String auctionProductId = auctionBidRequestDto.getAuctionProductId();
    Long bidPrice = auctionBidRequestDto.getBidPrice();
    bidInfoHistoryRepository.findById(BidInfoHistoryId.of(auctionProductId, bidPrice))
        .ifPresent(bidInfoHistory -> {
          throw new SameBidPriceException();
        });

    // 3. DynamoDB 저장
    bidInfoHistoryRepository.save(auctionBidRequestDto.to(consumerId));

    // 4. 입찰 내역 저장
    ZSetOperations<String, AuctionBidHistoryDto> bidHistoryRedis = redisTemplate.opsForZSet();

    AuctionBidHistoryDto historyDto = AuctionBidHistoryDto
        .of(memberDto, auctionProductId, bidPrice);

    bidHistoryRedis.add(auctionProductId, historyDto, bidPrice);

    // 5. 입찰 완료 토픽 발행
    kafkaBidInfoTemplate.send(BID_FIN_TOPIC, auctionProductId);
  }

  public AuctionBroadcastResponseDto enterAuction(Long consumerId, String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    // TODO : Circuitbreaker
    MemberDto memberDto = client.getConsumerInfo(consumerId).getData().to(consumerId);
    ValueOperations<String, MemberDto> memberRedis = redisTemplate.opsForValue();
    memberRedis.set(consumerId.toString(), memberDto);

    return AuctionBroadcastResponseDto.of(auction);
  }

  public void sendMessageToKafka(ChatMessageDto message, String auctionId) {
    ValueOperations<String, MemberDto> memberRedis = redisTemplate.opsForValue();
    MemberDto memberDto = memberRedis.get(message.getMemberId());

    kafkaChatTemplate.send(CHAT_TOPIC,
        KafkaChatMessageDto.toKafkaChatMessageDto(message, memberDto, auctionId));
  }

  @KafkaListener(topics = "CHAT_TOPIC")
  public void subMessage(KafkaChatMessageDto message) {
    template.convertAndSend("/sub/chat/" + message.getAuctionId(), message);
  }

  @KafkaListener(topics = "BID_FIN_TOPIC")
  public void subBidInfo(String auctionProductId) {
    // 경매 상품 입찰 내역 조회
    ZSetOperations<String, AuctionBidHistoryDto> bidHistoryRedis = redisTemplate.opsForZSet();
    List<AuctionBidHistoryDto> list = new ArrayList<>(
        Objects.requireNonNullElse(
            bidHistoryRedis.reverseRange(auctionProductId, 0, -1),
            Collections.emptyList()
        )
    );

    // 경매 상품 호가 조회
    ValueOperations<String, Long> askingPriceRedis = redisTemplate.opsForValue();
    Long askingPrice = askingPriceRedis.get("asking_price_" + auctionProductId);

    // 입찰 내역, 호가 전달
    template.convertAndSend("/sub/bid-info/" + KafkaAuctionBidHistoryDto.of(list, askingPrice));
  }
}
