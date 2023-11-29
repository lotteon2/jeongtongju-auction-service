package com.jeontongju.auction.enums;

import lombok.Getter;

@Getter
public enum AuctionStatusEnum {
  BEFORE("진행 예정"),
  ING("진행 중"),
  AFTER("진행 완료");
  private final String value;

  AuctionStatusEnum(String value) {
    this.value = value;
  }
}
