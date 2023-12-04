package com.jeontongju.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 등록 가능한 라이브 경매 조회
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerAuctionResponseDto {
  private String auctionId;
  private String title;
  private Long currentParticipants;
  @Builder.Default
  private Long maxParticipants = 5L;
}
