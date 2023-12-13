package com.jeontongju.auction.util;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InitData {

  public Auction initAuction(String title, AuctionStatusEnum auctionStatusEnum) {
    return Auction.builder()
        .title(title)
        .description("복순도가 누가 가져갈 것 인가")
        .startDate(LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY)),
            LocalTime.of(17, 0)))
        .status(auctionStatusEnum)
        .build();
  }

  public List<AuctionProduct> initAuctionProduct(Auction auction) {
    List<AuctionProduct> list = new ArrayList<>();

    AuctionProduct auctionProduct = AuctionProduct.builder()
        .auction(auction)
        .name("복순도가")
        .startingPrice(10000L)
        .description("복순복순")
        .capacity(500L)
        .alcoholDegree(17.0)
        .thumbnailImageUrl("thumbnail_img")
        .sellerId(1L)
        .storeImageUrl("store_img")
        .storeName("덤보네")
        .storeEmail("email@gmail.com")
        .storePhoneNumber("010-0101-0101")
        .businessmanName("김덤보")
        .build();

    list.add(auctionProduct);
    list.add(
        auctionProduct.toBuilder().name("안동소주").status(AuctionProductStatusEnum.CONFIRM).build());
    list.add(auctionProduct.toBuilder().name("막걸리나").status(AuctionProductStatusEnum.DENY).build());

    return list;
  }

  public List<BidInfo> initBidInfo(Auction auction, AuctionProduct auctionProduct,
      AuctionProduct auctionProduct2) {
    List<BidInfo> list = new ArrayList<>();

    BidInfo bidInfo1 = BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct)
        .consumerId(1L)
        .bidPrice(10000L)
        .build();

    BidInfo bidInfo2 = BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct)
        .consumerId(1L)
        .bidPrice(11000L)
        .build();

    BidInfo bidInfo3 = BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct)
        .consumerId(2L)
        .bidPrice(12000L)
        .isBid(true)
        .build();

    BidInfo bidInfo4 = BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct2)
        .consumerId(1L)
        .bidPrice(12000L)
        .build();

    BidInfo bidInfo5 = BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct2)
        .consumerId(1L)
        .bidPrice(15000L)
        .isBid(true)
        .build();

    list.add(bidInfo1);
    list.add(bidInfo2);
    list.add(bidInfo3);
    list.add(bidInfo5);
    list.add(bidInfo4);

    return list;
  }
}
