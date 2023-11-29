package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.BidInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidInfoRepository extends JpaRepository<BidInfo, Long> {

}
