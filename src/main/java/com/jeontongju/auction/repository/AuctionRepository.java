package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, String> {

}
