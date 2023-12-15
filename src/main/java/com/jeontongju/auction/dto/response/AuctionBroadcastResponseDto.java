package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBroadcastResponseDto {
  private String auctionName;
  private AuctionStatusEnum status;

  public static AuctionBroadcastResponseDto of(Auction auction) {
    return AuctionBroadcastResponseDto.builder()
        .auctionName(auction.getTitle())
        .status(auction.getStatus())
        .build();
  }
}
