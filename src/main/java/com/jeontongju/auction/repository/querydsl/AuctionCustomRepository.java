package com.jeontongju.auction.repository.querydsl;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import java.time.LocalDate;
import java.util.Optional;

public interface AuctionCustomRepository {
  Optional<SellerAuctionResponseDto> findRegistrableAuction();
  Optional<Auction> findThisAuction();
  Long findDateOfWeek(LocalDate localDate);
  Long countByAuctionProductIsAllowed();
}
