package com.jeontongju.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// 관리자 라이브 경매 목록 조회
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAuctionResponseDto extends AuctionResponseDto {
  private Long wait;
  private Long allow;
  private Long deny;
  private Long participation;
}
