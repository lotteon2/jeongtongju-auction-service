package com.jeontongju.auction.service;

import com.jeontongju.auction.dto.kafka.KafkaAuctionBidInfoDto;
import com.jeontongju.auction.dto.kafka.KafkaChatMessageDto;
import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BroadcastingService {

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

}
