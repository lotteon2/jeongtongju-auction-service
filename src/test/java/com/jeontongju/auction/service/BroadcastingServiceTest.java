package com.jeontongju.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.InvalidAuctionStatusException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import com.jeontongju.auction.util.InitData;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
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
public class BroadcastingServiceTest {
  @Autowired
  private AuctionRepository auctionRepository;

  @Autowired
  private AuctionProductRepository auctionProductRepository;

  @Autowired
  private BidInfoRepository bidInfoRepository;

  @Autowired
  private BroadcastingService broadcastingService;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private InitData init;

  private Auction initAuction;
  private List<AuctionProduct> initProductList;
  private List<BidInfo> initBidInfoList;

  @BeforeEach
  void before() {
    initAuction = init.initAuction("제 20회 복순도가 경매대회", AuctionStatusEnum.BEFORE);
    auctionRepository.save(initAuction);
  }

  @Test
  @DisplayName("경매 방송 시작")
  void startAuction() {
    // 시작 전
    broadcastingService.startAuction(initAuction.getAuctionId());
    assertEquals(initAuction.getStatus(), AuctionStatusEnum.ING);

    // 진행 중
    assertThrows(InvalidAuctionStatusException.class, () ->
        broadcastingService.startAuction(initAuction.getAuctionId())
    );

    Auction afterAuction = init.initAuction("완료된 경매", AuctionStatusEnum.AFTER);
    auctionRepository.save(afterAuction);

    // 완료
    assertThrows(InvalidAuctionStatusException.class, () ->
        broadcastingService.startAuction(initAuction.getAuctionId())
    );
  }

  @Test
  @DisplayName("경매 방송 종료")
  void endAuction() {
    // 시작 전
    assertThrows(InvalidAuctionStatusException.class, () ->
        broadcastingService.endAuction(initAuction.getAuctionId())
    );

    // 진행 중
    broadcastingService.startAuction(initAuction.getAuctionId());
    assertEquals(initAuction.getStatus(), AuctionStatusEnum.ING);

    broadcastingService.endAuction(initAuction.getAuctionId());
    assertEquals(initAuction.getStatus(), AuctionStatusEnum.AFTER);

    // 완료
    assertThrows(InvalidAuctionStatusException.class, () ->
        broadcastingService.endAuction(initAuction.getAuctionId())
    );
  }

}
