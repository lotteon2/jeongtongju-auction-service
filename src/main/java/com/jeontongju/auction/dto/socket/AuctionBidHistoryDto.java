package com.jeontongju.auction.dto.socket;

import com.jeontongju.auction.dto.redis.AuctionBidHistoryRedisDto;
import com.jeontongju.auction.dto.response.BroadcastProductResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBidHistoryDto {

  private Long askingPrice;
  private List<AuctionBidHistoryRedisDto> bidHistoryList;
  private List<BroadcastProductResponseDto> auctionProductList;

  public static AuctionBidHistoryDto of(List<AuctionBidHistoryRedisDto> bidHistoryList,
      List<BroadcastProductResponseDto> auctionProductList,
      Long askingPrice) {
    return AuctionBidHistoryDto.builder()
        .bidHistoryList(bidHistoryList)
        .auctionProductList(auctionProductList)
        .askingPrice(askingPrice)
        .build();
  }
}
