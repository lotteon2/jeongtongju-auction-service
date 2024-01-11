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
  private Long startingPrice;

  @Builder.Default
  private BroadcastProgressEnum progress = BroadcastProgressEnum.BEFORE;

  public static BroadcastProductResponseDto to(AuctionProduct auctionProduct) {
    return BroadcastProductResponseDto.builder()
        .auctionProductId(auctionProduct.getAuctionProductId())
        .auctionProductName(auctionProduct.getName())
        .startingPrice(auctionProduct.getStartingPrice())
        .build();
  }

  public void proceedProgress() {
    this.progress = BroadcastProgressEnum.ING;
  }

  public void closeProgress() {
    this.progress = BroadcastProgressEnum.AFTER;
  }
}
