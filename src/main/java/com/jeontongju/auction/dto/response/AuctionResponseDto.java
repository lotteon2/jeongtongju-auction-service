package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionResponseDto {

  private String auctionId;
  private String title;
  private String description;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private AuctionStatusEnum status;

  public AuctionResponseDto(Auction auction) {
    this.auctionId = auction.getAuctionId();
    this.title = auction.getTitle();
    this.description = auction.getDescription();
    this.startDate = auction.getStartDate();
    this.endDate = Objects.isNull(auction.getEndDate()) ? null : auction.getEndDate();
    this.status = auction.getStatus();
  }
}
