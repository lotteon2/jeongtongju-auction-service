package com.jeontongju.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
  private Long alcoholDegree;
  private String productImageUrl;

  private Long sellerId;
  private String sellerName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String sellerEmail;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String storePhoneNumber;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String storeImageUrl;
}
