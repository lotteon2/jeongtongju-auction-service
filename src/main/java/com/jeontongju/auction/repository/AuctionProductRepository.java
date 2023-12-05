package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.AuctionProduct;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionProductRepository extends JpaRepository<AuctionProduct, String> {
  Page<AuctionProduct> findAuctionProductBySellerId(Long sellerId, Pageable pageable);
  Optional<AuctionProduct> findByName(String productName);
}
