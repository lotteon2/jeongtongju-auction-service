package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class InvalidConsumerCreditException extends RuntimeException {
  private static final String message = "크레딧이 유효하지 않습니다.";

  public InvalidConsumerCreditException() {
    super(message);
  }
}
