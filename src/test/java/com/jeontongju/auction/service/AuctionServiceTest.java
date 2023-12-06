package com.jeontongju.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.dto.request.AuctionModifyRequestDto;
import com.jeontongju.auction.dto.request.AuctionRegisterRequestDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    initAuction = initAuction("제 20회 복순도가 경매대회", AuctionStatusEnum.BEFORE);
    auctionRepository.save(initAuction);
  }

  @Test
  @DisplayName("셀러 등록 가능한 경매 조회 - 3개 경매 상품 중 1개만 승인 상태")
  void getRegistrableAuction() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    SellerAuctionResponseDto registrableAuction = auctionService.getRegistrableAuction();

    assertEquals(registrableAuction.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(registrableAuction.getCurrentParticipants(), 1);
  }

  @Test
  @DisplayName("셀러 출품 내역 조회 - 복순도가만 경매 완료하여 낙찰정보가 있는 상태")
  void getAuctionEntries() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);
    AuctionProduct auctionProduct = auctionProductRepository.findByName("복순도가").orElseThrow(
        EntityNotFoundException::new);

    initBidInfoList = initBidInfo(initAuction, auctionProduct);
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    Page<SellerAuctionEntriesResponseDto> auctionEntries = auctionService.getAuctionEntries(1L,
        PageRequest.of(0, 10));

    auctionEntries.forEach(dto -> {
      if (dto.getAuctionProductName().equals("복순도가")) {
        assertEquals(dto.getLastBidPrice(), 12000);
        assertEquals(dto.getTotalBid(), 3);
      } else {
        assertNull(dto.getLastBidPrice());
        assertNull(dto.getTotalBid());
      }
    });
  }

  @Test
  @DisplayName("관리자 특정 경매 상세 조회")
  void getAdminAuctionDetail() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    Auction auction2 = initAuction("제 19회 복순도가 경매대회", AuctionStatusEnum.AFTER);
    auctionRepository.save(auction2);

    List<AuctionProduct> initProductList2 = initAuctionProduct(auction2);
    auctionProductRepository.saveAll(initProductList2);

    initBidInfoList = initBidInfo(auction2, initProductList2.get(0));
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    AuctionDetailResponseDto auctionBeforeResponse = auctionService.getAdminAuctionDetail(
        initAuction.getAuctionId());

    AuctionDetailResponseDto auctionAfterResponse = auctionService.getAdminAuctionDetail(
        auction2.getAuctionId());

    assertEquals(auctionBeforeResponse.getProductList().get(0).getBusinessmanName(), "김덤보");

    AuctionProductBidResponseDto bidResponse = (AuctionProductBidResponseDto) auctionAfterResponse.getProductList()
        .get(0);

    assertEquals(bidResponse.getLastBidPrice(), 12000);
  }

  @Test
  @DisplayName("소비자 이번 주 열리는 경매 상세 조회")
  void getThisAuctionDetail() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    entityManager.flush();
    entityManager.clear();

    AuctionDetailResponseDto thisAuctionDetail = auctionService.getThisAuctionDetail();

    assertEquals(thisAuctionDetail.getAuction().getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(thisAuctionDetail.getProductList().get(0).getSellerName(), "덤보네");
  }

  @Test
  @DisplayName("경매 생성")
  void registerAuction() {
    AuctionRegisterRequestDto request = AuctionRegisterRequestDto.builder()
        .title("제 30회 경매")
        .description("경매 생성 테스트")
        .startDate(LocalDateTime.of(2023, 12, 9, 17, 0))
        .build();

    auctionService.registerAuction(request);

    Auction result = auctionRepository.findByTitle("제 30회 경매")
        .orElseThrow(AuctionNotFoundException::new);

    assertEquals(result.getDescription(), "경매 생성 테스트");

  }

  @Test
  @DisplayName("경매 수정")
  void modifyAuction() {
    AuctionModifyRequestDto request = AuctionModifyRequestDto.builder()
        .title("제 31회 경매")
        .build();

    auctionService.modifyAuction(request, initAuction.getAuctionId());

    assertEquals(initAuction.getTitle(), "제 31회 경매");
    assertEquals(initAuction.getDescription(), "복순도가 누가 가져갈 것 인가");
  }

  private Auction initAuction(String title, AuctionStatusEnum auctionStatusEnum) {
    return Auction.builder()
        .title(title)
        .description("복순도가 누가 가져갈 것 인가")
        .startDate(LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY)),
            LocalTime.of(17, 0)))
        .status(auctionStatusEnum)
        .build();
  }

  @Test
  @DisplayName("경매 물품 승인")
  void approveAuctionProduct() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    auctionService.approveAuctionProduct(initProductList.get(0).getAuctionProductId(), true);
    auctionService.approveAuctionProduct(initProductList.get(1).getAuctionProductId(), false);

    assertEquals(initProductList.get(0).getStatus(), AuctionProductStatusEnum.ALLOW);
    assertEquals(initProductList.get(1).getStatus(), AuctionProductStatusEnum.DENY);
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
    list.add(
        auctionProduct.toBuilder().name("안동소주").status(AuctionProductStatusEnum.ALLOW).build());
    list.add(auctionProduct.toBuilder().name("막걸리나").status(AuctionProductStatusEnum.DENY).build());

    return list;
  }

  private List<BidInfo> initBidInfo(Auction auction, AuctionProduct auctionProduct) {
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
        .consumerId(2L)
        .bidPrice(11000L)
        .build();

    BidInfo bidInfo3 = BidInfo.builder()
        .auction(auction)
        .auctionProduct(auctionProduct)
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
