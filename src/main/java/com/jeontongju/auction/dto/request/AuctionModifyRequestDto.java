package com.jeontongju.auction.dto.request;

import com.jeontongju.auction.domain.Auction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionModifyRequestDto {

  @Nullable
  private String title;
  @Nullable
  private String description;

  public Auction toEntity(Auction auction) {
    Auction.AuctionBuilder auctionBuilder = auction.toBuilder();

    if (title != null) {
      auctionBuilder.title(title);
    }

    if (description != null) {
      auctionBuilder.description(description);
    }

    return auctionBuilder.build();
  }
}
