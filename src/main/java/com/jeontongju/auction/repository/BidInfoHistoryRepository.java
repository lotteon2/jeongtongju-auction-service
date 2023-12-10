package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.BidInfoHistory;
import com.jeontongju.auction.vo.BidInfoHistoryId;
import java.util.List;
import java.util.Optional;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface BidInfoHistoryRepository extends CrudRepository<BidInfoHistory, BidInfoHistoryId> {
  List<BidInfoHistory> findByAuctionProductId(String auctionProductId);
}
