package com.jeontongju.auction.controller;

import com.jeontongju.auction.service.AuctionService;
import io.github.bitbox.bitbox.dto.FeignFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuctionFeignController {

  private final AuctionService auctionService;

  @GetMapping("/auction-products/approval-wait")
  FeignFormat<Long> getApprovalWaitAuctionProducts() {
   return FeignFormat.<Long>builder()
       .code(HttpStatus.OK.value())
       .data(auctionService.getAllowProductCount())
       .build();
  }

}
