package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.AuctionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionProductRepository extends JpaRepository<AuctionProduct, String> {

}
