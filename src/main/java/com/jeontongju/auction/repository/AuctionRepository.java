package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.repository.querydsl.AuctionCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, String>, AuctionCustomRepository {

}
