package com.jeontongju.auction.controller;

import com.jeontongju.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuctionController {

  private final AuctionService auctionService;

  @GetMapping("/test")
  public String test() {
    return "test";
  }
}
