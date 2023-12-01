package com.jeontongju.auction.dto.kafka;

import com.jeontongju.auction.dto.request.AuctionBidRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaAuctionBidInfoDto {
  private Long memberId;
  private String auctionId;
  private String auctionProductId;
  private Long bidPrice;

  public static KafkaAuctionBidInfoDto toKafkaAuctionBidInfoDto(AuctionBidRequestDto auctionBidRequestDto, Long memberId) {
    return KafkaAuctionBidInfoDto.builder()
        .memberId(memberId)
        .auctionId(auctionBidRequestDto.getAuctionId())
        .auctionProductId(auctionBidRequestDto.getAuctionProductId())
        .bidPrice(auctionBidRequestDto.getBidPrice())
        .build();
  }
}
