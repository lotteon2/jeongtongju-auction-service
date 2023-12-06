package com.jeontongju.auction.controller;

import com.jeontongju.auction.dto.request.AuctionModifyRequestDto;
import com.jeontongju.auction.dto.request.AuctionProductRegisterRequestDto;
import com.jeontongju.auction.dto.request.AuctionRegisterRequestDto;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.dto.temp.ResponseFormat;
import com.jeontongju.auction.enums.temp.MemberRoleEnum;
import com.jeontongju.auction.service.AuctionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable
  ) {
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
      @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Page<AdminAuctionResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 목록 조회 성공")
                .data(auctionService.getAdminAuction(pageable))
                .build()
        );
  }

  @GetMapping("/admin/detail/{auctionId}")
  public ResponseEntity<ResponseFormat<AuctionDetailResponseDto>> getAdminAuctionDetail(
      @RequestHeader MemberRoleEnum memberRole, @PathVariable String auctionId
  ) {
    AuctionDetailResponseDto adminAuctionDetail = auctionService.getAdminAuctionDetail(auctionId);
    String message = "진행 예정 경매 조회 성공";
    switch (adminAuctionDetail.getAuction().getStatus()) {
      case ING:
        message = "진행 중 경매 조회 성공";
        break;
      case AFTER:
        message = "진행 완료 경매 조회 성공";
        break;
    }

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<AuctionDetailResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail(message)
                .data(adminAuctionDetail)
                .build()
        );
  }

  @GetMapping("/consumer/detatil")
  public ResponseEntity<ResponseFormat<AuctionDetailResponseDto>> getConsumerAuctionDetail() {

    return ResponseEntity.ok()
        .body(
            ResponseFormat.<AuctionDetailResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 조회 성공")
                .data(auctionService.getThisAuctionDetail())
                .build()
        );
  }

  @PostMapping("/product")
  public ResponseEntity<ResponseFormat<Void>> registerAuctionProduct(
      @Valid @RequestBody AuctionProductRegisterRequestDto request,
      @RequestHeader Long memberId) {
    auctionService.registerAuctionProduct(request, memberId);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 물품 등록 성공")
                .build()
        );
  }

  @PostMapping
  public ResponseEntity<ResponseFormat<Void>> registerAuction(
      @Valid @RequestBody AuctionRegisterRequestDto request
  ) {
    auctionService.registerAuction(request);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 생성 성공")
                .build()
        );
  }

  @PatchMapping("/{auctionId}")
  public ResponseEntity<ResponseFormat<Void>> modifyAuction(
      @PathVariable String auctionId, @RequestBody AuctionModifyRequestDto request
  ) {
    auctionService.modifyAuction(request, auctionId);
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("경매 수정 성공")
                .build()
        );
  }
}
