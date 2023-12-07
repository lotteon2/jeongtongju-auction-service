package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.BidInfo;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerAuctionBidResponseDto {
  private String auctionId;
  private String auctionName;
  private String productName;
  private String productImageUrl;
  private Long startingPrice;
  private Long lastBidPrice;
  private Long myLastBidPrice;
  private Boolean isBid;
  private LocalDate bidDate;

  public ConsumerAuctionBidResponseDto(BidInfo bidInfo) {
    this.auctionId = bidInfo.getAuction().getAuctionId();
    this.auctionName = bidInfo.getAuction().getTitle();
    this.productName = bidInfo.getAuctionProduct().getName();
    this.productImageUrl = bidInfo.getAuctionProduct().getThumbnailImageUrl();
    this.startingPrice = bidInfo.getAuctionProduct().getStartingPrice();
    this.myLastBidPrice = bidInfo.getBidPrice();
    this.isBid = bidInfo.getIsBid();
    this.bidDate = bidInfo.getCreatedAt().toLocalDate();
  }

  public void initLastBidPrice(Long lastBidPrice) {
    this.lastBidPrice = lastBidPrice;
  }
}
