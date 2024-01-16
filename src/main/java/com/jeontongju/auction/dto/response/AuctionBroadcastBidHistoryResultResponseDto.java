package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.dto.socket.BidHistoryInprogressDto;
import com.jeontongju.auction.dto.socket.BidResultListDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBroadcastBidHistoryResultResponseDto {
  private AuctionBroadcastResponseDto broadcastResponse;
  private BidHistoryInprogressDto bidHistory;
  private BidResultListDto bidResultList;

  public static AuctionBroadcastBidHistoryResultResponseDto of(
      AuctionBroadcastResponseDto auctionBroadcastResponseDto,
      BidHistoryInprogressDto bidHistoryInprogressDto,
      BidResultListDto bidResultList
  ) {
    return AuctionBroadcastBidHistoryResultResponseDto.builder()
        .broadcastResponse(auctionBroadcastResponseDto)
        .bidHistory(bidHistoryInprogressDto)
        .bidResultList(bidResultList)
        .build();
  }
}
