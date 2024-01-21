package com.jeontongju.auction.dto.redis;

import com.jeontongju.auction.util.Mosaic;
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
        .nickname(Mosaic.nameMosaic(memberDto.getNickname()))
        .profileImage(memberDto.getProfileImage())
        .auctionProductId(auctionProductId)
        .bidPrice(bidPrice)
        .build();
  }
}
