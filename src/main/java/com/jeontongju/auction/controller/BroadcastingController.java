package com.jeontongju.auction.controller;

import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import com.jeontongju.auction.dto.temp.ResponseFormat;
import com.jeontongju.auction.service.BroadcastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BroadcastingController {

  private final BroadcastingService broadcastingService;

  /**
   * 라이브 경매 채팅
   * @param message
   * @param auctionId
   */
  @MessageMapping("/chat/{auctionId}")
  public void pubMessage(ChatMessageDto message, @DestinationVariable("auctionId") String auctionId) {
    broadcastingService.sendMessageToKafka(message, auctionId);
  }

  /**
   * 라이브 경매 입찰
   * @param memberId
   * @param auctionBidRequestDto
   * @return
   */
  @PostMapping("/api/auction/bid")
  public ResponseEntity<ResponseFormat<Void>> bidProduct(
      @RequestHeader String memberId,
      @RequestBody AuctionBidRequestDto auctionBidRequestDto) {
    broadcastingService.sendBidInfoToKafka(auctionBidRequestDto, Long.parseLong(memberId));

    return ResponseEntity.ok().body(
        ResponseFormat.<Void>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .detail("경매 입찰 성공")
            .build());
  }
}
