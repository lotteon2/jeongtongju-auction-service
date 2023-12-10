package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class AuctionInvalidStatusException extends RuntimeException {
  private static final String message = "잘못 된 요청입니다.";

  public AuctionInvalidStatusException() {
    super(message);
  }

  public AuctionInvalidStatusException(String errorMessage) {
    super(errorMessage);
  }
}
