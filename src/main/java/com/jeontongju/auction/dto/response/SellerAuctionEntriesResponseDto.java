package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 셀러 라이브 경매 출품 내역 조회
@Getter
public class SellerAuctionEntriesResponseDto {

  private String auctionId;
  private String title;
  private LocalDateTime startDate;
  private String auctionProductName;
  private Long startingBidPrice;
  private Long lastBidPrice;
  private Long totalBid;
  private String auctionProductStatus;

  public SellerAuctionEntriesResponseDto(AuctionProduct auctionProduct) {
    this.auctionId = auctionProduct.getAuction().getAuctionId();
    this.title = auctionProduct.getAuction().getTitle();
    this.startDate = auctionProduct.getAuction().getStartDate();
    this.auctionProductName = auctionProduct.getName();
    this.startingBidPrice = auctionProduct.getStartingPrice();
    this.lastBidPrice = auctionProduct.getBidInfoList().isEmpty()
        ? null : auctionProduct.getBidInfoList()
        .stream()
        .max(Comparator.comparing(BidInfo::getBidPrice))
        .orElse(null)
        .getBidPrice();
    this.totalBid = auctionProduct.getBidInfoList().isEmpty()
        ? null : Long.valueOf(auctionProduct.getBidInfoList().size());
    this.auctionProductStatus = auctionProduct.getStatus().name();
  }
}
