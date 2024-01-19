package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class InvalidBidPriceException extends RuntimeException {
  private static final String message = "잘못 된 입찰 가격입니다.";

  public InvalidBidPriceException() {
    super(message);
  }
}
