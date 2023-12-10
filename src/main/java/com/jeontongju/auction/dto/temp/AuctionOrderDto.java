package com.jeontongju.auction.dto.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionOrderDto {

  Long consumerId;

  Long totalPrice;

  @Builder.Default
  Boolean isAuction = true;

  @Builder.Default
  Long paymentPrice = 0L;

  String productId;

  String productName;

  @Builder.Default
  Integer productCount = 1;

  Long productPrice;

  @Builder.Default
  Integer productTotalAmount = 1;

  Long sellerId;

  String sellerName;

  String productImg;

  public static AuctionOrderDto of(Long consumerId, Long totalPrice, String productId, String productName,
      Long productPrice, Long sellerId, String sellerName, String productImg) {
    return AuctionOrderDto.builder()
        .consumerId(consumerId)
        .totalPrice(totalPrice)
        .productId(productId)
        .productName(productName)
        .productPrice(productPrice)
        .sellerId(sellerId)
        .sellerName(sellerName)
        .productImg(productImg)
        .build();
  }
}
