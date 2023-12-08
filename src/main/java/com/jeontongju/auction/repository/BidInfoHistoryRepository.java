package com.jeontongju.auction.repository;

import com.jeontongju.auction.domain.BidInfoHistory;
import java.util.List;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface BidInfoHistoryRepository {
  List<BidInfoHistory> findByAuctionProductId(String auctionProductId);
}
