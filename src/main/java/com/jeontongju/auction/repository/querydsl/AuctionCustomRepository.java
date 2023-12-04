package com.jeontongju.auction.repository.querydsl;

import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import java.util.Optional;

public interface AuctionCustomRepository {
  Optional<SellerAuctionResponseDto> findRegistrableAuction();
}
