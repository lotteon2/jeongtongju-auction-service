package com.jeontongju.auction.repository.querydsl;

import com.jeontongju.auction.domain.BidInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BidInfoCustomRepository {

  Page<BidInfo> findByConsumerIdGroupByAuctionProduct(Long consumerId, Pageable pageable);
}
