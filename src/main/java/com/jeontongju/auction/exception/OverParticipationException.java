package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class OverParticipationException extends RuntimeException {
  private static final String message = "참여 인원이 초과되었습니다.";

  public OverParticipationException() {
    super(message);
  }
}
