package com.jeontongju.auction.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionProductBidResponseDto extends AuctionProductResponseDto {
  private Long consumerId;
  private Long lastBidPrice;
  private Long totalBid;
}
