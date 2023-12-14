package com.jeontongju.auction.dto.kafka;

import com.jeontongju.auction.dto.redis.AuctionBidHistoryDto;
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

  public static KafkaAuctionBidHistoryDto of(List<AuctionBidHistoryDto> bidHistoryList,
      Long askingPrice) {
    return KafkaAuctionBidHistoryDto.builder()
        .bidHistoryList(bidHistoryList)
        .askingPrice(askingPrice)
        .build();
  }
}
