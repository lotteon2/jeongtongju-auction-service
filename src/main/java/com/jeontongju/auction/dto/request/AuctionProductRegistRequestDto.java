package com.jeontongju.auction.dto.request;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.dto.temp.SellerInfoForAuctionDto;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionProductRegistRequestDto {

  @NotEmpty
  private String auctionId;

  @NotEmpty
  private String auctionProductName;

  @NotNull
  private Long startingPrice;

  @NotEmpty
  private String thumbnailImageUrl;

  @NotEmpty
  private String description;

  @NotNull
  private Long capacity;

  @NotNull
  private Double alcoholDegree;

  public AuctionProduct toEntity(Auction auction, SellerInfoForAuctionDto sellerInfo, Long sellerId) {
    return AuctionProduct.builder()
        .auction(auction)
        .name(auctionProductName)
        .startingPrice(startingPrice)
        .thumbnailImageUrl(thumbnailImageUrl)
        .description(description)
        .capacity(capacity)
        .alcoholDegree(alcoholDegree)
        .sellerId(sellerId)
        .storeName(sellerInfo.getStoreName())
        .storeImageUrl(sellerInfo.getStoreImageUrl())
        .storeEmail(sellerInfo.getStoreEmail())
        .storePhoneNumber(sellerInfo.getStorePhoneNumber())
        .businessmanName(sellerInfo.getBusinessmanName())
        .build();
  }
}
