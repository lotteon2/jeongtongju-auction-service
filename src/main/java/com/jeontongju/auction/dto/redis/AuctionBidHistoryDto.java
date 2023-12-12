package com.jeontongju.auction.dto.redis;

import io.github.bitbox.bitbox.dto.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionBidHistoryDto extends MemberDto {
  private String auctionProductId;
  private Long bidPrice;

  public static AuctionBidHistoryDto of(MemberDto memberDto, String auctionProductId, Long bidPrice) {
    return AuctionBidHistoryDto.builder()
        .memberId(memberDto.getMemberId())
        .profileImage(memberDto.getProfileImage())
        .auctionProductId(auctionProductId)
        .bidPrice(bidPrice)
        .build();
  }
}
