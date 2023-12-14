package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class SameWeekOfAuctionException extends RuntimeException {
  private static final String message = "해당 주차에 이미 경매일정이 존재합니다.";

  public SameWeekOfAuctionException() {
    super(message);
  }

}
