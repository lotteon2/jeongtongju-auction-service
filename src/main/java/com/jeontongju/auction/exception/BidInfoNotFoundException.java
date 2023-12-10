package com.jeontongju.auction.exception;

import org.jetbrains.annotations.Nullable;
import org.webjars.NotFoundException;

public class BidInfoNotFoundException extends NotFoundException {
  private static final String message = "입찰 정보를 찾을 수 없습니다.";

  public BidInfoNotFoundException(@Nullable String message) {
    super(message);
  }
}
