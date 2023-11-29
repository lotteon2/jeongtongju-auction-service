package com.jeontongju.auction.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionResponseDto {
  private String auctionId;
  private String title;
  private String description;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String status;
}
