package com.jeontongju.auction.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 특정 라이브 경매 상세 조회
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDetailResponseDto {
  private AuctionResponseDto auction;
  private List<? extends AuctionProductResponseDto> productList;
}
