package com.jeontongju.auction.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBidHistoryDto {
  private Long memberId;
  private String auctionProductId;
  private Long bidPrice;

  public static AuctionBidHistoryDto of(Long memberId, String auctionProductId, Long bidPrice) {
    return AuctionBidHistoryDto.builder()
        .memberId(memberId)
        .auctionProductId(auctionProductId)
        .bidPrice(bidPrice)
        .build();
  }
}
