package com.jeontongju.auction.service;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuctionServiceTest {

  @Autowired
  private AuctionRepository auctionRepository;

  @Autowired
  private AuctionProductRepository auctionProductRepository;

  @Autowired
  private BidInfoRepository bidInfoRepository;

  @Autowired
  private AuctionService auctionService;

  @Autowired
  private EntityManager entityManager;

  private Auction initAuction;
  private List<AuctionProduct> initProductList;
  private List<BidInfo> initBidInfoList;

  @BeforeEach
  void before() {
    initAuction = initAuction();
    auctionRepository.save(initAuction);
  }








  private Auction initAuction() {
    return Auction.builder()
        .title("제 20회 복순도가 경매대회")
        .description("복순도가 누가 가져갈 것 인가")
        .startDate(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)))
        .build();
  }

  private List<AuctionProduct> initAuctionProduct(Auction auction) {
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
    list.add(auctionProduct.toBuilder().name("안동소주").status(AuctionProductStatusEnum.ALLOW).build());
    list.add(auctionProduct.toBuilder().name("막걸리나").status(AuctionProductStatusEnum.DENY).build());

    return list;
  }

  private List<BidInfo> initBidInfo() {
    List<BidInfo> list = new ArrayList<>();

    BidInfo bidInfo1 = BidInfo.builder()
        .auction(initAuction)
        .consumerId(1L)
        .bidPrice(10000L)
        .build();

    BidInfo bidInfo2 = BidInfo.builder()
        .auction(initAuction)
        .consumerId(2L)
        .bidPrice(11000L)
        .build();

    BidInfo bidInfo3 = BidInfo.builder()
        .auction(initAuction)
        .consumerId(1L)
        .bidPrice(12000L)
        .isBid(true)
        .build();

    list.add(bidInfo1);
    list.add(bidInfo2);
    list.add(bidInfo3);

    return list;
  }
}
