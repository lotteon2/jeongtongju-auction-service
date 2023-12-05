package com.jeontongju.auction.exception;

import org.webjars.NotFoundException;

public class AuctionProductNotFoundException extends NotFoundException {
  private static final String message = "경매 상품 정보를 찾을 수 없습니다.";

  public AuctionProductNotFoundException() {
    super(message);
  }
}
