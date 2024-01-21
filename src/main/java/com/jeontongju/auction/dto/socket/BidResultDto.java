package com.jeontongju.auction.dto.socket;

import com.jeontongju.auction.util.Mosaic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResultDto {
  private Long consumerId;
  private String consumerName;
  private String auctionProductId;
  private String productName;
  private Long lastBidPrice;

  public static BidResultDto of(Long consumerId, String consumerName, String auctionProductId, String productName, Long lastBidPrice) {
    return BidResultDto.builder()
        .consumerId(consumerId)
        .consumerName(Mosaic.nameMosaic(consumerName))
        .auctionProductId(auctionProductId)
        .productName(productName)
        .lastBidPrice(lastBidPrice)
        .build();
  }
}
