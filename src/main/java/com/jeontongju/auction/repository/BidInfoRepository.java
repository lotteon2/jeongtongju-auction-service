package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.BidInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidInfoRepository extends JpaRepository<BidInfo, Long> {
  List<BidInfo> findByConsumerId(Long consumerId);
  List<BidInfo> findAllByIsBidTrue();
}
