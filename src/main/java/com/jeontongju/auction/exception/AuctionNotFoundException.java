package com.jeontongju.auction.exception;

import com.jeontongju.auction.exception.common.EntityNotFoundException;

public class AuctionNotFoundException extends EntityNotFoundException {
  private static final String message = "경매 정보를 찾을 수 없습니다.";

  public AuctionNotFoundException() {
    super(message);
  }
}
