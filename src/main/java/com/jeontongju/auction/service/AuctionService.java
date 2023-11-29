package com.jeontongju.auction.service;

import com.jeontongju.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionService {

  private AuctionRepository auctionRepository;

}
