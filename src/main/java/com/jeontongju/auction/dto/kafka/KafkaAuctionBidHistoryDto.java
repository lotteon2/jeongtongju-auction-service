package com.jeontongju.auction.dto.kafka;

import com.jeontongju.auction.dto.redis.AuctionBidHistoryDto;
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
public class KafkaAuctionBidHistoryDto {

  private Long askingPrice;
  private List<AuctionBidHistoryDto> bidHistoryList;
  private List<BroadcastProductResponseDto> auctionProductList;

  public static KafkaAuctionBidHistoryDto of(List<AuctionBidHistoryDto> bidHistoryList,
      List<BroadcastProductResponseDto> auctionProductList,
      Long askingPrice) {
    return KafkaAuctionBidHistoryDto.builder()
        .bidHistoryList(bidHistoryList)
        .auctionProductList(auctionProductList)
        .askingPrice(askingPrice)
        .build();
  }
}
