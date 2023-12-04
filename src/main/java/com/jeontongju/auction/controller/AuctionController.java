package com.jeontongju.auction.controller;

import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.dto.temp.ResponseFormat;
import com.jeontongju.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuctionController {

  private final AuctionService auctionService;

  @GetMapping("/auction/seller")
  public ResponseEntity<ResponseFormat<SellerAuctionResponseDto>> getRegistrableAuction() {
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<SellerAuctionResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("등록 가능한 경매 조회 성공")
                .data(auctionService.getRegistrableAuction())
                .build()
        );
  }
}
