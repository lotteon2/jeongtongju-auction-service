package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.enums.BroadcastProgressEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastProductResponseDto {
  private String auctionProductId;
  private String auctionProductName;

  @Builder.Default
  private BroadcastProgressEnum progress = BroadcastProgressEnum.BEFORE;

  public BroadcastProductResponseDto(AuctionProduct auctionProduct) {
    this.auctionProductId = auctionProduct.getAuctionProductId();
    this.auctionProductName = auctionProduct.getName();
  }
}
