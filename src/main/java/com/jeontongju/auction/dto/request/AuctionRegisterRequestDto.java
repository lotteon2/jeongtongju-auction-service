package com.jeontongju.auction.dto.request;

import com.jeontongju.auction.domain.Auction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionRegisterRequestDto {

  @NotEmpty
  private String title;

  @NotEmpty
  private String description;

  @NotNull
  private String startDate;

  public Auction toEntity() {
    return Auction.builder()
        .title(title)
        .description(description)
        .startDate(LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy.MM.dd.")).atTime(17, 0))
        .build();
  }
}
