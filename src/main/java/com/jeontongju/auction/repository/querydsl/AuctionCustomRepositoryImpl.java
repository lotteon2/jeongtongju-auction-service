package com.jeontongju.auction.repository.querydsl;

import static com.jeontongju.auction.domain.QAuction.auction;
import static com.jeontongju.auction.domain.QAuctionProduct.auctionProduct;
import static org.hibernate.internal.util.NullnessHelper.coalesce;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionCustomRepositoryImpl implements AuctionCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<SellerAuctionResponseDto> findRegistrableAuction() {
    SellerAuctionResponseDto result = jpaQueryFactory
        .select(
            Projections.fields(
                SellerAuctionResponseDto.class,
                auction.auctionId,
                auction.title,
                Expressions.as(auctionProduct.count(), "currentParticipants")
            )
        )
        .from(auction)
        .leftJoin(auction.auctionProductList, auctionProduct)
        .where(
            auction.isDeleted.isFalse(),
            auction.status.eq(AuctionStatusEnum.BEFORE),
            auctionProduct.status.eq(AuctionProductStatusEnum.ALLOW).or(auctionProduct.status.isNull())
        )
        .orderBy(auction.startDate.desc())
        .limit(1)
        .groupBy(auction.auctionId)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<Auction> findThisAuction() {
    LocalDate currentDate = LocalDate.now();
    int theDaysLeft = DayOfWeek.FRIDAY.getValue() - currentDate.getDayOfWeek().getValue();
    if (theDaysLeft < 0) {
      theDaysLeft += 7;
    }
    LocalDate nextFriday = currentDate.plusDays(theDaysLeft);

    Auction result = jpaQueryFactory
        .selectFrom(auction)
        .where(
            auction.isDeleted.isFalse(),
            auction.startDate.after(nextFriday.atStartOfDay())
        )
        .orderBy(auction.startDate.asc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }

}
