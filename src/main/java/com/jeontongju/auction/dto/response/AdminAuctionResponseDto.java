package com.jeontongju.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// 관리자 라이브 경매 목록 조회
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAuctionResponseDto extends AuctionResponseDto {

  private Long wait;
  private Long allow;
  private Long deny;
  private Long participation;

  public AdminAuctionResponseDto(Auction auction) {
    super(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
        auction.getStartDate(), auction.getEndDate(), auction.getStatus());

    if (auction.getStatus().equals(AuctionStatusEnum.AFTER)) {
      this.participation = countFilteredNumbers(auction.getAuctionProductList(),
          auctionProduct -> auctionProduct.getStatus() == AuctionProductStatusEnum.ALLOW);
    } else {
      this.wait = countFilteredNumbers(auction.getAuctionProductList(),
          auctionProduct -> auctionProduct.getStatus() == AuctionProductStatusEnum.WAIT);
      this.allow = countFilteredNumbers(auction.getAuctionProductList(),
          auctionProduct -> auctionProduct.getStatus() == AuctionProductStatusEnum.ALLOW);
      this.deny = countFilteredNumbers(auction.getAuctionProductList(),
          auctionProduct -> auctionProduct.getStatus() == AuctionProductStatusEnum.DENY);
    }
  }

  public <T> Long countFilteredNumbers(List<T> list,
      java.util.function.Predicate<? super T> predicate) {
    if (Objects.isNull(list))
      return null;
    return list.stream().filter(predicate).count();
  }
}
