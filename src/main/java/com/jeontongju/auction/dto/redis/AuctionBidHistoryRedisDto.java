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
public class AuctionBidHistoryRedisDto extends MemberDto {
  private String auctionProductId;
  private Long bidPrice;

  public static AuctionBidHistoryRedisDto of(MemberDto memberDto, String auctionProductId, Long bidPrice) {
    return AuctionBidHistoryRedisDto.builder()
        .memberId(memberDto.getMemberId())
        .nickname(memberDto.getNickname())
        .profileImage(memberDto.getProfileImage())
        .auctionProductId(auctionProductId)
        .bidPrice(bidPrice)
        .build();
  }
}
