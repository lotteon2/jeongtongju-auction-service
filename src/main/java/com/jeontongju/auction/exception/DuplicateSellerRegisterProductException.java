package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class DuplicateSellerRegisterProductException extends RuntimeException {
  private static final String message = "이미 경매에 상품을 등록했습니다.";

  public DuplicateSellerRegisterProductException() {
    super(message);
  }
}
