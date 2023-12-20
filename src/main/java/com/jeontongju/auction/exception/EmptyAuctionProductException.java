package com.jeontongju.auction.exception;

import lombok.Getter;

@Getter
public class EmptyAuctionProductException extends RuntimeException{
  private static final String message = "경매 물품이 없습니다.";

  public EmptyAuctionProductException() {
    super(message);
  }
}
