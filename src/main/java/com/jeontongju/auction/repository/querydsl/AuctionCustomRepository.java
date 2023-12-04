package com.jeontongju.auction.repository.querydsl;

import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionCustomRepository {
  Optional<SellerAuctionResponseDto> findRegistrableAuction();
}
