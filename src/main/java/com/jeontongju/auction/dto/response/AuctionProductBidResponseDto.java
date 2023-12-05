package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.exception.AuctionProductNotFoundException;
import java.util.Comparator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionProductBidResponseDto extends AuctionProductResponseDto {

  private Long consumerId;
  private Long lastBidPrice;
  private Long totalBid;

  public AuctionProductBidResponseDto(AuctionProduct auctionProduct) {
    super(
        auctionProduct.getAuctionProductId(),
        auctionProduct.getName(),
        auctionProduct.getDescription(),
        auctionProduct.getStartingPrice(),
        auctionProduct.getCapacity(),
        auctionProduct.getAlcoholDegree(),
        auctionProduct.getThumbnailImageUrl(),
        auctionProduct.getSellerId(),
        auctionProduct.getStoreName()
    );
    this.consumerId = auctionProduct.getBidInfoList().isEmpty() ? null
        : auctionProduct.getBidInfoList().stream().max(Comparator.comparing(BidInfo::getBidPrice))
            .get()
            .getConsumerId();
    this.lastBidPrice = auctionProduct.getBidInfoList().isEmpty() ? null
        : auctionProduct.getBidInfoList().stream().max(Comparator.comparing(BidInfo::getBidPrice))
            .get()
            .getBidPrice();
    this.totalBid = auctionProduct.getBidInfoList().isEmpty() ? null
        : (long) auctionProduct.getBidInfoList().size();
  }
}
