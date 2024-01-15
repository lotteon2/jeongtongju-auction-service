package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.dto.socket.AuctionBidHistoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBroadcastBidHistoryResponseDto {
  private AuctionBroadcastResponseDto auctionBroadcastResponseDto;
  private AuctionBidHistoryDto auctionBidHistoryDto;

  public static AuctionBroadcastBidHistoryResponseDto of(
      AuctionBroadcastResponseDto auctionBroadcastResponseDto, AuctionBidHistoryDto auctionBidHistoryDto
  ) {
    return AuctionBroadcastBidHistoryResponseDto.builder()
        .auctionBroadcastResponseDto(auctionBroadcastResponseDto)
        .auctionBidHistoryDto(auctionBidHistoryDto)
        .build();
  }
}
