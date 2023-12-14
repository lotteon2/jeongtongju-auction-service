package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class InvalidAuctionStatusException extends RuntimeException {
  private static final String message = "잘못 된 요청입니다.";

  public InvalidAuctionStatusException() {
    super(message);
  }

  public InvalidAuctionStatusException(String errorMessage) {
    super(errorMessage);
  }
}
