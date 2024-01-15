package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.dto.socket.BidHistoryInprogressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBroadcastBidHistoryResponseDto {
  private AuctionBroadcastResponseDto broadcastResponse;
  private BidHistoryInprogressDto bidHistory;

  public static AuctionBroadcastBidHistoryResponseDto of(
      AuctionBroadcastResponseDto auctionBroadcastResponseDto, BidHistoryInprogressDto bidHistoryInprogressDto
  ) {
    return AuctionBroadcastBidHistoryResponseDto.builder()
        .broadcastResponse(auctionBroadcastResponseDto)
        .bidHistory(bidHistoryInprogressDto)
        .build();
  }
}
