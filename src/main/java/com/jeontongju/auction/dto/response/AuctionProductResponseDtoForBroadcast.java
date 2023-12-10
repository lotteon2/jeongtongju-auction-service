package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.AuctionProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionProductResponseDtoForBroadcast {
  private String auctionProductId;
  private String auctionProductName;

  public AuctionProductResponseDtoForBroadcast(AuctionProduct auctionProduct) {
    this.auctionProductId = auctionProduct.getAuctionProductId();
    this.auctionProductName = auctionProduct.getName();
  }
}
