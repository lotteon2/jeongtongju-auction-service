package com.jeontongju.auction.enums;

import lombok.Getter;

@Getter
public enum AuctionProductStatusEnum {
  WAIT("대기"),
  CONFIRM("승인"),
  DENY("거절 ");
  private final String value;

  AuctionProductStatusEnum(String value) {
    this.value = value;
  }
}
