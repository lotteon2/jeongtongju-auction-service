package com.jeontongju.auction.enums;

import lombok.Getter;

@Getter
public enum BroadcastProgressEnum {
  BEFORE("준비"),
  ING("진행중"),
  AFTER("마감");
  private final String value;

  BroadcastProgressEnum(String value) {
    this.value = value;
  }
}
