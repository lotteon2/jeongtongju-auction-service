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

  @Value("${chatTopic}")
  private String chatTopic;

  @Value("${bidInfoTopic}")
  private String bidInfoTopic;

  public void sendMessageToKafka(ChatMessageDto message, String auctionId) {
    kafkaChatTemplate.send(chatTopic, KafkaChatMessageDto.toKafkaChatMessageDto(message, auctionId));
  }

  public void sendBidInfoToKafka(AuctionBidRequestDto auctionBidRequestDto, Long memberId) {
    kafkaBidInfoTemplate.send(bidInfoTopic, KafkaAuctionBidInfoDto.toKafkaAuctionBidInfoDto(auctionBidRequestDto, memberId));
  }

  @KafkaListener(topics = "${chatTopic}")
  public void subMessage(KafkaChatMessageDto message) {
    template.convertAndSend("/sub/chat/" + message.getAuctionId(), message);
  }

  @KafkaListener(topics = "${bidInfoTopic}")
  public void subBidInfo(KafkaAuctionBidInfoDto bidInfo) {
    template.convertAndSend("/sub/bid-info/" + bidInfo.getAuctionId(), bidInfo);
  }

}
