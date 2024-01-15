package com.jeontongju.auction.service;

import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.BID_CHAT;
import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.BID_INFO;
import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.BID_RESULT;
import static io.github.bitbox.bitbox.util.KafkaTopicNameInfo.CREATE_AUCTION_ORDER;

import com.jeontongju.auction.client.ConsumerServiceFeignClient;
import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.domain.BidInfoHistory;
import com.jeontongju.auction.dto.redis.AuctionBidHistoryDto;
import com.jeontongju.auction.dto.request.ChatMessageRequestDto;
import com.jeontongju.auction.dto.response.AuctionBroadcastBidHistoryResponseDto;
import com.jeontongju.auction.dto.socket.BidHistoryInprogressDto;
import com.jeontongju.auction.dto.socket.BidResultDto;
import com.jeontongju.auction.dto.socket.BidResultListDto;
import com.jeontongju.auction.dto.socket.ChatMessageDto;
import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.response.AuctionBroadcastResponseDto;
import com.jeontongju.auction.dto.response.BroadcastProductResponseDto;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.exception.AuctionProductNotFoundException;
import com.jeontongju.auction.exception.EmptyAuctionProductException;
import com.jeontongju.auction.exception.InvalidAuctionStatusException;
import com.jeontongju.auction.exception.InvalidConsumerCreditException;
import com.jeontongju.auction.exception.SameBidPriceException;
import com.jeontongju.auction.kafka.KafkaProcessor;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoHistoryRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import com.jeontongju.auction.vo.BidInfoHistoryId;
import io.github.bitbox.bitbox.dto.AuctionOrderDto;
import io.github.bitbox.bitbox.dto.MemberDto;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastingService {

  private final AuctionRepository auctionRepository;
  private final AuctionProductRepository auctionProductRepository;
  private final BidInfoRepository bidInfoRepository;
  private final BidInfoHistoryRepository bidInfoHistoryRepository;

  @Qualifier("redisStringTemplate")
  private final RedisTemplate redisTemplate;

  @Qualifier("redisGenericTemplate")
  private final RedisTemplate redisGenericTemplate;

  @Value("profile.img")
  private String profileImg;

  private static final Long TTL = 6L;

  private final KafkaProcessor kafkaProcessor;
  private final SimpMessagingTemplate template;

  private final ConsumerServiceFeignClient client;

  public void startAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.AFTER)) {
      throw new InvalidAuctionStatusException("이미 완료된 경매입니다.");
    }

    if (auction.getAuctionProductList().isEmpty()) {
      throw new EmptyAuctionProductException();
    }

    if (!status.equals(AuctionStatusEnum.ING)) {
      List<BroadcastProductResponseDto> productList = auction.getAuctionProductList()
          .stream()
          .map(BroadcastProductResponseDto::to)
          .collect(Collectors.toList());

      productList.get(0).proceedProgress();

      ValueOperations<String, List<BroadcastProductResponseDto>> auctionProductRedis = redisGenericTemplate.opsForValue();
      auctionProductRedis.set("auction_id_" + auctionId, productList, TTL, TimeUnit.HOURS);

      ValueOperations<String, Integer> productIdx = redisTemplate.opsForValue();
      productIdx.set(auctionId + "_index", 0, TTL, TimeUnit.HOURS);
    }
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

    auctionRepository.save(
        auction.toBuilder()
            .status(AuctionStatusEnum.AFTER)
            .endDate(LocalDateTime.now())
            .build()
    );
  }

  public void bidProduct(AuctionBidRequestDto auctionBidRequestDto, Long consumerId) {
    String auctionId = auctionBidRequestDto.getAuctionId();
    // 1. 크레딧 검사
    ValueOperations<String, MemberDto> memberRedis = redisGenericTemplate.opsForValue();
    MemberDto memberDto = memberRedis.get("consumer_id_" + consumerId);
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
    ZSetOperations<String, AuctionBidHistoryDto> bidHistoryRedis = redisGenericTemplate.opsForZSet();

    AuctionBidHistoryDto historyDto = AuctionBidHistoryDto
        .of(memberDto, auctionProductId, bidPrice);

    bidHistoryRedis.add("auction_product_id" + auctionProductId, historyDto, bidPrice);
    redisGenericTemplate.expire("auction_product_id" + auctionProductId, TTL, TimeUnit.HOURS);

    // 5. 입찰 완료 토픽 발행
    kafkaProcessor.send(BID_INFO, auctionId);
  }

  public AuctionBroadcastBidHistoryResponseDto enterAuction(Long consumerId,
      MemberRoleEnum memberRoleEnum,
      String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    setCredit(consumerId, memberRoleEnum);

    return AuctionBroadcastBidHistoryResponseDto.of(
        AuctionBroadcastResponseDto.of(auction),
        getPublishingBidHistory(auctionId)
    );
  }

  public void modifyAskingPrice(String auctionId, Long askingPrice) {
    // 진행 중인 경매 물품 조회
    String auctionProductId = getAuctionProductIdFromRedis(auctionId);

    // 호가 수정
    ValueOperations<String, Long> askingPriceRedis = redisTemplate.opsForValue();
    askingPriceRedis.set("asking_price_" + auctionProductId, askingPrice, TTL, TimeUnit.HOURS);

    // 수정된 호가로 경매 정보 전송
    kafkaProcessor.send(BID_INFO, auctionId);

    // 수정된 호가 안내 메시지 전송
    kafkaProcessor.send(BID_CHAT,
        setNotificationMessage(auctionId, updateAskingPriceMessage(askingPrice)));
  }

  @Transactional
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
    kafkaProcessor.send(
        CREATE_AUCTION_ORDER,
        AuctionOrderDto.of(
            successfulBid.getConsumerId(), successfulBid.getBidPrice(),
            auctionProductId, auctionProduct.getName(),
            successfulBid.getBidPrice(), auctionProduct.getSellerId(),
            auctionProduct.getStoreName(), auctionProduct.getThumbnailImageUrl(),
            1L
        )
    );

    // 6. 입찰 내역 삭제
    redisTemplate.delete("auction_product_id" + auctionProductId);
    redisTemplate.delete("asking_price_" + auctionProductId);

    // 7. 진행도 다음으로 수정
    ValueOperations<String, Integer> productIdx = redisTemplate.opsForValue();
    int index = productIdx.get(auctionId + "_index");
    productIdx.set(auctionId + "_index", index + 1);

    productList.get(index).closeProgress();
    if (index < productList.size() - 1) {
      productList.get(index + 1).proceedProgress();
    }

    // 8. Kafka 정보 전송
    ValueOperations<String, List<BroadcastProductResponseDto>> auctionProductRedis = redisGenericTemplate.opsForValue();
    auctionProductRedis.set("auction_id_" + auctionId, productList, TTL, TimeUnit.HOURS);

    kafkaProcessor.send(BID_INFO, auctionId);

    ValueOperations<String, MemberDto> memberRedis = redisGenericTemplate.opsForValue();
    MemberDto memberDto = memberRedis.get("consumer_id_" + successfulBid.getConsumerId());
    String nickname = memberDto.getNickname();

    kafkaProcessor.send(BID_CHAT,
        setNotificationMessage(auctionId, successfulBidMessage(nickname)));

    ValueOperations<String, BidResultListDto> bidResultRedis = redisGenericTemplate.opsForValue();
    BidResultListDto bidResultListDto = Objects.requireNonNullElse(
        bidResultRedis.get("bid_result_" + auctionId),
        BidResultListDto.create(auctionId)
    );

    bidResultListDto.addResult(
        BidResultDto.of(
            successfulBid.getConsumerId(),
            nickname,
            auctionProduct.getName(),
            successfulBid.getBidPrice()
        )
    );

    bidResultRedis.set("bid_result_" + auctionId, bidResultListDto, TTL, TimeUnit.HOURS);
    kafkaProcessor.send(BID_RESULT, bidResultListDto);
  }

  public void sendMessageToKafka(ChatMessageRequestDto message, String auctionId) {
    MemberDto memberDto;
    if (message.getMemberId() == 0) {
      memberDto = MemberDto.builder().memberId(0L).nickname("관리자").profileImage("").credit(0L)
          .build();
    } else {
      ValueOperations<String, MemberDto> memberRedis = redisGenericTemplate.opsForValue();
      memberDto = memberRedis.get("consumer_id_" + message.getMemberId());
    }
    kafkaProcessor.send(BID_CHAT,
        ChatMessageDto.toKafkaChatMessageDto(message, memberDto, auctionId));
  }

  @KafkaListener(topics = BID_CHAT)
  public void pubMessage(ChatMessageDto message) {
    template.convertAndSend("/sub/chat/" + message.getAuctionId(), message);
  }

  @KafkaListener(topics = BID_INFO)
  public void pubBidInfo(String auctionId) {
    // 입찰 내역, 호가 전달
    template.convertAndSend("/sub/bid-info/" + auctionId, getPublishingBidHistory(auctionId));
  }

  @KafkaListener(topics = BID_RESULT)
  public void pubBidResult(BidResultListDto bidResultListDto) {
    template.convertAndSend("/sub/bid-result/" + bidResultListDto.getAuctionId(), bidResultListDto);
  }

  public BidHistoryInprogressDto getPublishingBidHistory(String auctionId) {
    List<BroadcastProductResponseDto> productList = getAuctionProductListFromRedis(auctionId);
    String auctionProductId = getAuctionProductIdFromRedis(auctionId);

    // 경매 상품 입찰 내역 조회
    ZSetOperations<String, AuctionBidHistoryDto> bidHistoryRedis = redisGenericTemplate.opsForZSet();
    List<AuctionBidHistoryDto> bidHistoryList = new ArrayList<>(
        Objects.requireNonNullElse(
            bidHistoryRedis.reverseRange("auction_product_id" + auctionProductId, 0, 4),
            Collections.emptyList()
        )
    );

    // 경매 상품 호가 조회
    ValueOperations<String, Long> askingPriceRedis = redisTemplate.opsForValue();
    Long askingPrice = Objects.requireNonNullElse(
        askingPriceRedis.get("asking_price_" + auctionProductId), 0L);

    return BidHistoryInprogressDto.of(bidHistoryList, productList, askingPrice);
  }

  public void setCredit(Long consumerId, MemberRoleEnum memberRoleEnum) {
    if (consumerId != null & memberRoleEnum != null) {
      if (!memberRoleEnum.equals(MemberRoleEnum.ROLE_ADMIN)) {
        MemberDto memberDto = client.getConsumerInfo(consumerId).getData().to(consumerId);
        ValueOperations<String, MemberDto> memberRedis = redisGenericTemplate.opsForValue();
        memberRedis.set("consumer_id_" + consumerId, memberDto, TTL, TimeUnit.HOURS);
      }
    } else {
      throw new InvalidConsumerCreditException();
    }
  }

  private List<BroadcastProductResponseDto> getAuctionProductListFromRedis(String auctionId) {
    ValueOperations<String, List<BroadcastProductResponseDto>> auctionProductRedis = redisGenericTemplate.opsForValue();
    return auctionProductRedis.get("auction_id_" + auctionId);
  }

  private String getAuctionProductIdFromRedis(String auctionId) {
    ValueOperations<String, Integer> productIdx = redisTemplate.opsForValue();
    int index = productIdx.get(auctionId + "_index");

    List<BroadcastProductResponseDto> auctionProductList = getAuctionProductListFromRedis(
        auctionId);

    int size = auctionProductList.size();
    if (index >= size) {
      return auctionProductList.get(size - 1).getAuctionProductId();
    }

    return auctionProductList.get(index).getAuctionProductId();
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

  private ChatMessageDto setNotificationMessage(String auctionId, String chatMessage) {
    return ChatMessageDto.to(
        auctionId,
        -1L,
        "\uD83C\uDF76 전통주점",
        profileImg,
        chatMessage
    );
  }

  private String updateAskingPriceMessage(Long askingPrice) {
    return "❗호가가 [" + NumberFormat.getInstance(Locale.KOREA).format(askingPrice) + "]원으로 변경되었어요!";
  }

  private String successfulBidMessage(String nickname) {
    return "🎉 [" + nickname + "]님이 낙찰되었어요!";
  }
}
