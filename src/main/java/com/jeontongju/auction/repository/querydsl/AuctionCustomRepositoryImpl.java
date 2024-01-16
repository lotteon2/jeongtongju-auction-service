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
                Expressions.cases()
                    .when(auctionProduct.status.eq(AuctionProductStatusEnum.ALLOW)).then(1L)
                    .otherwise(0L)
                    .sum().as("currentParticipants")
            )
        )
        .from(auction)
        .leftJoin(auction.auctionProductList, auctionProduct)
        .where(
            auction.isDeleted.isFalse(),
            auction.status.eq(AuctionStatusEnum.BEFORE),
            auction.startDate.after(
                getTargetAuctionDate(LocalDate.now(), DayOfWeek.WEDNESDAY).atStartOfDay())
        )
        .orderBy(auction.startDate.asc())
        .limit(1)
        .groupBy(auction.auctionId)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  // 매 주 월요일 열리는 경매
  @Override
  public Optional<Auction> findThisAuction() {
    Auction result = jpaQueryFactory
        .selectFrom(auction)
        .where(
            auction.isDeleted.isFalse(),
            auction.startDate.after(
                getTargetAuctionDate(LocalDate.now(), DayOfWeek.MONDAY).atStartOfDay()
            )
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

  @Override
  public Long countByAuctionProductIsWait() {
    return jpaQueryFactory.select(auctionProduct.count())
        .from(auctionProduct)
        .where(auctionProduct.status.eq(AuctionProductStatusEnum.WAIT))
        .fetchOne();
  }

  // 가장 최근 열리는 경매 (임시)
  @Override
  public Optional<Auction> findThisAuctionRecent() {
    Auction result = jpaQueryFactory
        .selectFrom(auction)
        .where(
            auction.isDeleted.isFalse()
        )
        .orderBy(auction.createdAt.desc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  // 가장 최근 열리는 경매 (임시)
  @Override
  public Optional<SellerAuctionResponseDto> findRegistrableAuctionRecent() {
    SellerAuctionResponseDto result = jpaQueryFactory
        .select(
            Projections.fields(
                SellerAuctionResponseDto.class,
                auction.auctionId,
                auction.title,
                Expressions.cases()
                    .when(auctionProduct.status.eq(AuctionProductStatusEnum.ALLOW)).then(1L)
                    .otherwise(0L)
                    .sum().as("currentParticipants")
            )
        )
        .from(auction)
        .leftJoin(auction.auctionProductList, auctionProduct)
        .where(
            auction.isDeleted.isFalse(),
            auction.status.eq(AuctionStatusEnum.BEFORE)
        )
        .orderBy(auction.createdAt.desc())
        .limit(1)
        .groupBy(auction.auctionId)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  private LocalDate getTargetAuctionDate(LocalDate today, DayOfWeek dayOfWeek) {
    if (today.getDayOfWeek().compareTo(dayOfWeek) <= 0) {
      return today.with(dayOfWeek);
    } else {
      return today.plusDays(7).with(dayOfWeek);
    }
  }

}
