package com.jeontongju.auction.repository.querydsl;

import static com.jeontongju.auction.domain.QAuction.auction;
import static com.jeontongju.auction.domain.QAuctionProduct.auctionProduct;
import static org.hibernate.internal.util.NullnessHelper.coalesce;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
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
                ExpressionUtils.as(
                    new CaseBuilder()
                        .when(auctionProduct.status.eq(AuctionProductStatusEnum.ALLOW)).then(auctionProduct.count())
                        .otherwise(0L),
                    "currentParticipants"
                )
            )
        )
        .from(auction)
        .leftJoin(auction.auctionProductList, auctionProduct)
        .where(
            auction.isDeleted.isFalse(),
            auction.status.eq(AuctionStatusEnum.BEFORE)
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

  @Override
  public Long findDateOfWeek(LocalDate localDate) {
    return jpaQueryFactory.select(auction.count())
        .from(auction)
        .where(
            auction.startDate.week().eq(localDate.get(WeekFields.ISO.weekOfWeekBasedYear())),
            auction.isDeleted.isFalse()
        )
        .fetchOne();
  }

}
