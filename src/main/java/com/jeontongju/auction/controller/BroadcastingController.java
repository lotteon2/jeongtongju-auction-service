package com.jeontongju.auction.controller;

import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import com.jeontongju.auction.dto.request.ChatMessageDto;
import com.jeontongju.auction.dto.response.AuctionBroadcastResponseDto;
import com.jeontongju.auction.service.BroadcastingService;
import io.github.bitbox.bitbox.dto.ResponseFormat;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
   *
   * @param message
   * @param auctionId
   */
  @MessageMapping("/chat/{auctionId}")
  public void pubMessage(ChatMessageDto message,
      @DestinationVariable("auctionId") String auctionId) {
    broadcastingService.sendMessageToKafka(message, auctionId);
  }

  /**
   * 라이브 경매 입찰
   *
   * @param memberId
   * @param auctionBidRequestDto
   * @return
   */

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
    broadcastingService.endAuction(auctionId);
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
      @RequestHeader(required = false) Long consumerId,
      @RequestHeader(required = false) MemberRoleEnum memberRole,
      @PathVariable String auctionId) {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<AuctionBroadcastResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 방 입장 성공")
                .data(broadcastingService.enterAuction(consumerId, memberRole, auctionId))
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
}
