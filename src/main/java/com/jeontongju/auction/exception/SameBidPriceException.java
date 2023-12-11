package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class SameBidPriceException extends RuntimeException {
  private static final String message = "이미 입찰된 가격입니다.";

  public SameBidPriceException() {
    super(message);
  }
}
