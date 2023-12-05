package com.jeontongju.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionProductResponseDto {

  private String auctionProductId;
  private String productName;
  private String description;
  private Long startingPrice;
  private Long capacity;
  private Double alcoholDegree;
  private String productImageUrl;
  private Long sellerId;
  private String sellerName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String sellerEmail;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String businessmanName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String storePhoneNumber;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String storeImageUrl;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private AuctionProductStatusEnum status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalDateTime createdAt;

  public AuctionProductResponseDto(AuctionProduct auctionProduct) {
    this.auctionProductId = auctionProduct.getAuctionProductId();
    this.productName = auctionProduct.getName();
    this.description = auctionProduct.getDescription();
    this.startingPrice = auctionProduct.getStartingPrice();
    this.capacity = auctionProduct.getCapacity();
    this.alcoholDegree = auctionProduct.getAlcoholDegree();
    this.productImageUrl = auctionProduct.getThumbnailImageUrl();
    this.sellerId = auctionProduct.getSellerId();
    this.sellerName = auctionProduct.getStoreName();
    this.sellerEmail = auctionProduct.getStoreEmail();
    this.businessmanName = auctionProduct.getBusinessmanName();
    this.storeImageUrl = auctionProduct.getStoreImageUrl();
    this.status = auctionProduct.getStatus();
    this.createdAt = auctionProduct.getCreatedAt();
  }

  public AuctionProductResponseDto(String auctionProductId, String productName, String description,
      Long startingPrice, Long capacity, Double alcoholDegree, String productImageUrl,
      Long sellerId, String sellerName) {
    this.auctionProductId = auctionProductId;
    this.productName = productName;
    this.description = description;
    this.startingPrice = startingPrice;
    this.capacity = capacity;
    this.alcoholDegree = alcoholDegree;
    this.productImageUrl = productImageUrl;
    this.sellerId = sellerId;
    this.sellerName = sellerName;
  }
}
