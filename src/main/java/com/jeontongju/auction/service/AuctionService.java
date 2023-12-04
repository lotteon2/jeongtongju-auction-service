package com.jeontongju.auction.service;

import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {

  private final AuctionRepository auctionRepository;

  public SellerAuctionResponseDto getRegistrableAuction() {
    return auctionRepository.findRegistrableAuction().orElseThrow(AuctionNotFoundException::new);
  }

}
