package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.Auction;
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
  private String auctionId;
  private List<BroadcastProductResponseDto> auctionProductList;

  public static AuctionBroadcastResponseDto of(Auction auction) {
    return AuctionBroadcastResponseDto.builder()
        .auctionId(auction.getAuctionId())
        .auctionProductList(
            auction.getAuctionProductList()
                .stream()
                .map(BroadcastProductResponseDto::new)
                .collect(Collectors.toList())
        )
        .build();
  }
}
