package com.jeontongju.auction.service;

import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.BID_CHAT;
import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.BID_INFO;
import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.CREATE_AUCTION_ORDER;

import com.jeontongju.auction.client.ConsumerServiceFeignClient;
import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.domain.BidInfoHistory;
import com.jeontongju.auction.dto.kafka.KafkaAuctionBidHistoryDto;
import com.jeontongju.auction.dto.kafka.KafkaChatMessageDto;
import com.jeontongju.auction.dto.redis.AuctionBidHistoryDto;
import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import com.jeontongju.auction.dto.response.AuctionBroadcastResponseDto;
import com.jeontongju.auction.dto.response.BroadcastProductResponseDto;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.exception.AuctionProductNotFoundException;
import com.jeontongju.auction.exception.EmptyAuctionProductException;
import com.jeontongju.auction.exception.InvalidAuctionStatusException;
import com.jeontongju.auction.exception.InvalidConsumerCreditException;
import com.jeontongju.auction.exception.SameBidPriceException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoHistoryRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import com.jeontongju.auction.vo.BidInfoHistoryId;
import io.github.bitbox.bitbox.dto.AuctionOrderDto;
import io.github.bitbox.bitbox.dto.MemberDto;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
  private final AuctionProductRepository auctionProductRepository;
  private final BidInfoRepository bidInfoRepository;
  private final BidInfoHistoryRepository bidInfoHistoryRepository;

  private final RedisTemplate redisTemplate;
  private static final Long TTL = 6L;

  private final SimpMessagingTemplate template;
  private final KafkaTemplate<String, KafkaChatMessageDto> kafkaChatTemplate;
  private final KafkaTemplate<String, String> kafkaBidInfoTemplate;
  private final KafkaTemplate<String, AuctionOrderDto> kafkaOrderTemplate;

  private final ConsumerServiceFeignClient client;

  public void startAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.ING)) {
      throw new InvalidAuctionStatusException("이미 진행 중인 경매입니다.");
    } else if (status.equals(AuctionStatusEnum.AFTER)) {
      throw new InvalidAuctionStatusException("이미 완료된 경매입니다.");
    }

    if (auction.getAuctionProductList().isEmpty()) {
      throw new EmptyAuctionProductException();
    }

    List<BroadcastProductResponseDto> list = auction.getAuctionProductList()
        .stream()
        .map(BroadcastProductResponseDto::new)
        .collect(Collectors.toList());

    list.get(0).proceedProgress();

    ValueOperations<String, List<BroadcastProductResponseDto>> auctionProductRedis = redisTemplate.opsForValue();
    auctionProductRedis.set(auctionId, list, TTL);

    ValueOperations<String, Integer> productIdx = redisTemplate.opsForValue();
    productIdx.set(auctionId + "_index", 1, TTL);

    auctionRepository.save(auction.toBuilder().status(AuctionStatusEnum.ING).build());
  }

  public void endAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.BEFORE)) {
      throw new InvalidAuctionStatusException("경매가 시작하지 않았습니다.");
    } else if (status.equals(AuctionStatusEnum.AFTER)) {
      throw new InvalidAuctionStatusException("이미 완료된 경매입니다.");
    }

    auctionRepository.save(auction.toBuilder().status(AuctionStatusEnum.AFTER).build());
  }

  public void bidProduct(AuctionBidRequestDto auctionBidRequestDto, Long consumerId) {
    String auctionId = auctionBidRequestDto.getAuctionId();
    // 1. 크레딧 검사
    ValueOperations<Long, MemberDto> memberRedis = redisTemplate.opsForValue();
    MemberDto memberDto = memberRedis.get(consumerId);
    Long memberCredit = memberDto.getCredit();

    if (memberCredit == null || memberCredit < auctionBidRequestDto.getBidPrice()) {
      throw new InvalidConsumerCreditException();
    }

    // 2. 동일 상품 + 동일 입찰가 데이터 검사
    String auctionProductId = getAuctionProductIdFromRedis(auctionId);

    Long bidPrice = auctionBidRequestDto.getBidPrice();
    bidInfoHistoryRepository.findById(BidInfoHistoryId.of(auctionProductId, bidPrice))
        .ifPresent(bidInfoHistory -> {
          throw new SameBidPriceException();
        });

    // 3. DynamoDB 저장
    bidInfoHistoryRepository.save(auctionBidRequestDto.to(consumerId, auctionProductId));

    // 4. 입찰 내역 저장
    // TODO : Redis Util
    ZSetOperations<String, AuctionBidHistoryDto> bidHistoryRedis = redisTemplate.opsForZSet();

    AuctionBidHistoryDto historyDto = AuctionBidHistoryDto
        .of(memberDto, auctionProductId, bidPrice);

    bidHistoryRedis.add(auctionProductId, historyDto, bidPrice);
    redisTemplate.expire(auctionProductId, TTL, TimeUnit.HOURS);

    // 5. 입찰 완료 토픽 발행
    kafkaBidInfoTemplate.send(BID_INFO, auctionId);
  }

  public AuctionBroadcastResponseDto enterAuction(Long consumerId, MemberRoleEnum memberRoleEnum,
      String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    if (consumerId != null & memberRoleEnum != null) {
      if (!memberRoleEnum.equals(MemberRoleEnum.ROLE_ADMIN)) {
        MemberDto memberDto = client.getConsumerInfo(consumerId).getData().to(consumerId);
        ValueOperations<Long, MemberDto> memberRedis = redisTemplate.opsForValue();
        memberRedis.set(consumerId, memberDto, TTL, TimeUnit.HOURS);
      }
    }

    return AuctionBroadcastResponseDto.of(auction);
  }

  public void modifyAskingPrice(String auctionId, Long askingPrice) {
    String auctionProductId = getAuctionProductIdFromRedis(auctionId);

    ValueOperations<String, Long> askingPriceRedis = redisTemplate.opsForValue();
    askingPriceRedis.set("asking_price_" + auctionProductId, askingPrice, TTL, TimeUnit.HOURS);

    kafkaBidInfoTemplate.send(BID_INFO, auctionId);
  }

  public void successfulBid(String auctionId) {
    List<BroadcastProductResponseDto> productList = getAuctionProductListFromRedis(auctionId);
    String auctionProductId = getAuctionProductIdFromRedis(auctionId);

    // 1. 경매 물품 입찰 내역 조회
    List<BidInfoHistory> bidInfoHistoryList = bidInfoHistoryRepository
        .findByAuctionProductId(auctionProductId);

    // 2. 입찰 내역이 없을 시 반환
    if (bidInfoHistoryList.isEmpty()) {
      return;
    }

    // 3. 낙찰 내역에 해당하는 유저 크레딧 차감
    BidInfoHistory successfulBid = bidInfoHistoryList.get(bidInfoHistoryList.size() - 1);
    client.deductCredit(successfulBid.getConsumerId(), successfulBid.getBidPrice());

    // 4. RDB 내역 저장
    Auction auction = auctionRepository.findById(successfulBid.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);
    AuctionProduct auctionProduct = auctionProductRepository.findById(auctionProductId)
        .orElseThrow(AuctionProductNotFoundException::new);

    List<BidInfo> list = convert(bidInfoHistoryList, auction, auctionProduct);
    int idx = list.size() - 1;
    list.set(idx, list.get(idx).toBuilder().isBid(true).build());
    bidInfoRepository.saveAll(list);

    // 5. 주문 카프카 발행
    kafkaOrderTemplate.send(
        CREATE_AUCTION_ORDER,
        AuctionOrderDto.of(
            successfulBid.getConsumerId(), successfulBid.getBidPrice(),
            auctionProductId, auctionProduct.getName(),
            successfulBid.getBidPrice(), auctionProduct.getSellerId(),
            auctionProduct.getStoreName(), auctionProduct.getStoreImageUrl()
        )
    );

    // 6. 입찰 내역 삭제
    ZSetOperations<String, AuctionBidHistoryDto> bidHistoryRedis = redisTemplate.opsForZSet();
    bidHistoryRedis.remove(auctionProductId);
    redisTemplate.delete("asking_price_" + auctionProductId);

    // 7. 진행도 다음으로 수정
    ValueOperations<String, Integer> productIdx = redisTemplate.opsForValue();
    int index = productIdx.get(auctionId + "_index");

    productList.get(index).closeProgress();
    if (index < productList.size() - 1) {
      productList.get(index + 1).proceedProgress();
    }

    kafkaBidInfoTemplate.send(BID_INFO, auctionId);
  }

  public void sendMessageToKafka(ChatMessageDto message, String auctionId) {
    MemberDto memberDto;
    if (message.getMemberId() == 0) {
      memberDto = MemberDto.builder().memberId(0L).nickname("관리자").profileImage("").credit(0L)
          .build();
    } else {
      ValueOperations<Long, MemberDto> memberRedis = redisTemplate.opsForValue();
      memberDto = memberRedis.get(message.getMemberId());
    }
    kafkaChatTemplate.send(BID_CHAT,
        KafkaChatMessageDto.toKafkaChatMessageDto(message, memberDto, auctionId));
  }

  @KafkaListener(topics = BID_CHAT)
  public void subMessage(KafkaChatMessageDto message) {
    template.convertAndSend("/sub/chat/" + message.getAuctionId(), message);
  }

  @KafkaListener(topics = BID_INFO)
  public void subBidInfo(String auctionId) {
    List<BroadcastProductResponseDto> productList = getAuctionProductListFromRedis(auctionId);
    String auctionProductId = getAuctionProductIdFromRedis(auctionId);

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
    template.convertAndSend(
        "/sub/bid-info/" + KafkaAuctionBidHistoryDto.of(list, productList, askingPrice));
  }

  private List<BroadcastProductResponseDto> getAuctionProductListFromRedis(String auctionId) {
    ValueOperations<String, List<BroadcastProductResponseDto>> auctionProductRedis = redisTemplate.opsForValue();
    return auctionProductRedis.get(auctionId);
  }

  private String getAuctionProductIdFromRedis(String auctionId) {
    ValueOperations<String, Integer> productIdx = redisTemplate.opsForValue();
    return getAuctionProductListFromRedis(auctionId).get(productIdx.get(auctionId + "_index"))
        .getAuctionProductId();
  }

  private List<BidInfo> convert(List<BidInfoHistory> list, Auction auction,
      AuctionProduct auctionProduct) {
    return list.stream()
        .map(history -> historyConvertToBidInfo(history, auction, auctionProduct))
        .collect(Collectors.toList());
  }

  private BidInfo historyConvertToBidInfo(BidInfoHistory bidInfoHistory, Auction auction,
      AuctionProduct auctionProduct) {
    return BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct)
        .bidPrice(bidInfoHistory.getBidPrice())
        .consumerId(bidInfoHistory.getConsumerId())
        .build();
  }
}
