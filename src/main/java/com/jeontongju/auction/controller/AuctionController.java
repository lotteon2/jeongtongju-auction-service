package com.jeontongju.auction.controller;

import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.dto.temp.ResponseFormat;
import com.jeontongju.auction.enums.temp.MemberRoleEnum;
import com.jeontongju.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auction")
public class AuctionController {

  private final AuctionService auctionService;

  @GetMapping("/seller")
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

  @GetMapping("/detail/seller")
  public ResponseEntity<ResponseFormat<Page<SellerAuctionEntriesResponseDto>>> getAuctionEntries(
      @RequestHeader Long memberId, @RequestHeader MemberRoleEnum memberRole,
      @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable) {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Page<SellerAuctionEntriesResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 출품 내역 조회 성공")
                .data(auctionService.getAuctionEntries(memberId, pageable))
                .build()
        );
  }

  @GetMapping("/admin")
  public ResponseEntity<ResponseFormat<Page<AdminAuctionResponseDto>>> getAdminAuction(
      @RequestHeader MemberRoleEnum memberRole,
      @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable) {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Page<AdminAuctionResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 출품 내역 조회 성공")
                .data(auctionService.getAdminAuction(pageable))
                .build()
        );
  }
}
