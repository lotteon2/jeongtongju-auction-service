package com.jeontongju.auction.dto.response;

import com.jeontongju.auction.enums.AuctionStatusEnum;
import java.time.LocalDateTime;
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
}
