package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 특정 라이브 경매 상세 조회
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDetailResponseDto {

  private AuctionResponseDto auction;
  private List<? extends AuctionProductResponseDto> productList;

  public static AuctionDetailResponseDto of(Auction auction) {
    return AuctionDetailResponseDto.builder()
        .auction(
            Optional.of(auction).map(AuctionResponseDto::new)
                .orElseThrow(AuctionNotFoundException::new)
        )
        .productList(
            auction.getAuctionProductList() == null ? null :
                auction.getAuctionProductList().stream()
                    .map(
                        auction.getStatus() == AuctionStatusEnum.AFTER ?
                            AuctionProductBidResponseDto::new
                            : AuctionProductResponseDto::new
                    ).collect(
                        Collectors.toList())
        )
        .build();
  }
}
