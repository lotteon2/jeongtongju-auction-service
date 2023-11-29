package com.jeontongju.auction.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 셀러 라이브 경매 출품 내역 조회
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerAuctionEntriesResponseDto {
  private String auctionId;
  private String auctionProductName;
  private String title;
  private LocalDateTime startDate;
  private Long lastBidPrice;
  private Long startingBidPrice;
  private String auctionProductStatus;
}
