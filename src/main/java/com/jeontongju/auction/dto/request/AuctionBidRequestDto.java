package com.jeontongju.auction.dto.request;

import com.jeontongju.auction.domain.BidInfoHistory;
import com.jeontongju.auction.vo.BidInfoHistoryId;
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
  private Long bidPrice;

  public BidInfoHistory to(Long consumerId, String auctionProductId) {
    return BidInfoHistory.builder()
        .bidInfoHistoryId(
            BidInfoHistoryId.builder()
                .auctionProductId(auctionProductId)
                .bidPrice(bidPrice)
                .build()
        )
        .auctionId(auctionId)
        .consumerId(consumerId)
        .build();
  }
}
