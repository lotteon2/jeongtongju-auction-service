package com.jeontongju.auction.dto.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerInfoForAuctionDto {

  private String storeName;
  private String storeImageUrl;
  private String storeEmail;
  private String storePhoneNumber;
  private String businessmanName;

}
