package com.jeontongju.auction.service;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.kafka.KafkaAuctionBidInfoDto;
import com.jeontongju.auction.dto.kafka.KafkaChatMessageDto;
import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionInvalidStatusException;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BroadcastingService {

  private final AuctionRepository auctionRepository;
  private final AuctionProductRepository auctionProductRepository;

  private final SimpMessagingTemplate template;
  private final KafkaTemplate<String, KafkaChatMessageDto> kafkaChatTemplate;
  private final KafkaTemplate<String, KafkaAuctionBidInfoDto> kafkaBidInfoTemplate;

  private static final String CHAT_TOPIC = "bid-chat-topic";
  private static final String BID_INFO_TOPIC = "bid-info-topic";

  public void sendMessageToKafka(ChatMessageDto message, String auctionId) {
    kafkaChatTemplate.send(CHAT_TOPIC, KafkaChatMessageDto.toKafkaChatMessageDto(message, auctionId));
  }

  public void sendBidInfoToKafka(AuctionBidRequestDto auctionBidRequestDto, Long memberId) {
    kafkaBidInfoTemplate.send(BID_INFO_TOPIC, KafkaAuctionBidInfoDto.toKafkaAuctionBidInfoDto(auctionBidRequestDto, memberId));
  }

  @KafkaListener(topics = "CHAT_TOPIC")
  public void subMessage(KafkaChatMessageDto message) {
    template.convertAndSend("/sub/chat/" + message.getAuctionId(), message);
  }

  @KafkaListener(topics = "BID_INFO_TOPIC")
  public void subBidInfo(KafkaAuctionBidInfoDto bidInfo) {
    template.convertAndSend("/sub/bid-info/" + bidInfo.getAuctionId(), bidInfo);
  }

  public void startAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId).orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.ING)) {
      throw new AuctionInvalidStatusException("이미 진행 중인 경매입니다.");
    } else if (status.equals(AuctionStatusEnum.AFTER)){
      throw new AuctionInvalidStatusException("이미 완료된 경매입니다.");
    }

    auctionRepository.save(auction.toBuilder().status(AuctionStatusEnum.ING).build());
  }

  public void endAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId).orElseThrow(AuctionNotFoundException::new);

    AuctionStatusEnum status = auction.getStatus();
    if (status.equals(AuctionStatusEnum.BEFORE)) {
      throw new AuctionInvalidStatusException("경매가 시작하지 않았습니다.");
    } else if (status.equals(AuctionStatusEnum.AFTER)){
      throw new AuctionInvalidStatusException("이미 완료된 경매입니다.");
    }

    auctionRepository.save(auction.toBuilder().status(AuctionStatusEnum.AFTER).build());
  }
}
