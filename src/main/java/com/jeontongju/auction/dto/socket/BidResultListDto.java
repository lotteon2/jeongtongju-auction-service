package com.jeontongju.auction.dto.socket;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResultListDto {
  String auctionId;
  List<BidResultDto> bidResult;

  public void addResult(BidResultDto bidResultDto) {
    this.bidResult.add(bidResultDto);
  }

  public static BidResultListDto create(String auctionId) {
    return BidResultListDto.builder()
        .auctionId(auctionId)
        .bidResult(new ArrayList<>())
        .build();
  }
}
