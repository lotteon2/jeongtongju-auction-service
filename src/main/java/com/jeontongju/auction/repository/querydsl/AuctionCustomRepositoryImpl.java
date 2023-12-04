package com.jeontongju.auction.repository.querydsl;

import static com.jeontongju.auction.domain.QAuction.auction;
import static com.jeontongju.auction.domain.QAuctionProduct.auctionProduct;

import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionCustomRepositoryImpl implements AuctionCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<SellerAuctionResponseDto> findRegistrableAuction() {
    String latestAuctionId = jpaQueryFactory
        .select(auction.auctionId)
        .from(auction)
        .where(auction.status.eq(AuctionStatusEnum.BEFORE))
        .orderBy(auction.startDate.desc())
        .limit(1)
        .fetchOne();

    SellerAuctionResponseDto result = jpaQueryFactory
        .select(
            Projections.constructor(
                SellerAuctionResponseDto.class,
                auction.auctionId,
                auction.title,
                auctionProduct.count().as("currentParticipants")
            )
        )
        .from(auction)
        .join(auction.auctionProductList)
        .fetchJoin()
        .where(
            auction.isDeleted.isFalse(),
            auction.auctionId.eq(latestAuctionId)
        )
        .groupBy(auction.auctionId)
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
