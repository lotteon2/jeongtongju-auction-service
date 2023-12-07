package com.jeontongju.auction.repository.querydsl;

import static com.jeontongju.auction.domain.QBidInfo.bidInfo;

import com.jeontongju.auction.domain.BidInfo;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BidInfoCustomRepositoryImpl implements BidInfoCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<BidInfo> findByConsumerIdGroupByAuctionProduct(Long consumerId, Pageable pageable) {
    JPAQuery<BidInfo> query = jpaQueryFactory
        .selectFrom(bidInfo)
        .where(bidInfo.consumerId.eq(consumerId));

    List<BidInfo> results = query.fetch();

    JPAQuery<Long> countQuery = jpaQueryFactory
        .select(bidInfo.count())
        .from(bidInfo)
        .where(bidInfo.consumerId.eq(consumerId));

    return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
  }
}
