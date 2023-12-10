package com.jeontongju.auction.dto.kafka;

import com.jeontongju.auction.dto.redis.AuctionBidHistoryDto;
import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaAuctionBidInfoDto {

  private Long askingPrice;
  private List<AuctionBidHistoryDto> bidHistoryList;

  public static KafkaAuctionBidInfoDto of(List<AuctionBidHistoryDto> bidHistoryList,
      Long askingPrice) {
    return KafkaAuctionBidInfoDto.builder()
        .bidHistoryList(bidHistoryList)
        .askingPrice(askingPrice)
        .build();
  }
}
