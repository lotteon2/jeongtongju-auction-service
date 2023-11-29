package com.jeontongju.auction.dto.response;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsumerAuctionBidResponseDto {
  private String auctionId;
  private String auctionName;
  private String productImageUrl;
  private String startingPrice;
  private String lastBidPrice;
  private String myLastBidPrice;
  private String isBid;
  private LocalDate bidDate;
}
