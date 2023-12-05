package com.jeontongju.auction.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  AuctionBidRequestDto {
  private String auctionId;
  private String auctionProductId;
  private Long bidPrice;
}