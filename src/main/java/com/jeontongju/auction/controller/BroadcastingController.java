package com.jeontongju.auction.controller;

import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import com.jeontongju.auction.dto.response.AuctionBroadcastResponseDto;
import com.jeontongju.auction.service.BroadcastingService;
import io.github.bitbox.bitbox.dto.ResponseFormat;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BroadcastingController {

  private final BroadcastingService broadcastingService;

  @MessageMapping("/chat/{auctionId}")
  public void pubMessage(ChatMessageDto message,
      @DestinationVariable("auctionId") String auctionId) {
    broadcastingService.sendMessageToKafka(message, auctionId);
  }

  @SubscribeMapping("/bid-info/{auctionId}")
  public void pubInitBidInfo(@DestinationVariable("auctionId") String auctionId, SimpMessageHeaderAccessor headerAccessor) {
    String sessionId = headerAccessor.getSessionId();
    broadcastingService.pubBidInfo(sessionId, auctionId);
  }

  @PostMapping("/api/auction/bid")
  public ResponseEntity<ResponseFormat<Void>> bidProduct(
      @RequestHeader Long memberId,
      @RequestBody AuctionBidRequestDto auctionBidRequestDto) {
    broadcastingService.bidProduct(auctionBidRequestDto, memberId);
    return ResponseEntity.ok().body(
        ResponseFormat.<Void>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .detail("경매 입찰 성공")
            .build());
  }

  @PostMapping("/api/auction/streaming/{auctionId}")
  public ResponseEntity<ResponseFormat<Void>> startStreaming(
      @PathVariable String auctionId
  ) {
    broadcastingService.startAuction(auctionId);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 방송 생성 성공")
                .build()
        );
  }

  @PatchMapping("/api/auction/streaming/{auctionId}")
  public ResponseEntity<ResponseFormat<Void>> endStreaming(
      @PathVariable String auctionId
  ) {
    
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 방송 종료 성공")
                .build()
        );
  }

  @GetMapping("/api/auction/room/{auctionId}")
  public ResponseEntity<ResponseFormat<AuctionBroadcastResponseDto>> enterStreaming(
      @RequestHeader(required = false) Long memberId,
      @RequestHeader(required = false) MemberRoleEnum memberRole,
      @PathVariable String auctionId) {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<AuctionBroadcastResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 방 입장 성공")
                .data(broadcastingService.enterAuction(memberId, memberRole, auctionId))
                .build()
        );
  }

  @PatchMapping("/api/auction/bid/{auctionId}/askingPrice/{askingPrice}")
  public ResponseEntity<ResponseFormat<Void>> modifyAskingPrice(
      @PathVariable String auctionId, @PathVariable Long askingPrice
  ) {
    broadcastingService.modifyAskingPrice(auctionId, askingPrice);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 호가 수정 성공")
                .build()
        );
  }

  @PostMapping("/api/auction/bid/{auctionId}")
  public ResponseEntity<ResponseFormat<Void>> successfulBid(@PathVariable String auctionId) {
    broadcastingService.successfulBid(auctionId);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 낙찰 성공")
                .build()
        );
  }

  @EventListener
  public void connectEvent(SessionConnectEvent sessionConnectEvent){
    StompHeaderAccessor headers = StompHeaderAccessor.wrap(sessionConnectEvent.getMessage());

    String sessionId = headers.getSessionId();
    log.info("연결 성공, {}", sessionConnectEvent);
    log.info("세션 ID {}", sessionId);
  }

  @EventListener
  public void onDisconnectEvent(SessionDisconnectEvent sessionDisconnectEvent) {
    log.info("연결 해제, {}", sessionDisconnectEvent);
  }
}
