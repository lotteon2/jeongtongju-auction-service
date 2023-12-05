package com.jeontongju.auction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto.AuctionDetailResponseDtoBuilder;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductResponseDto;
import com.jeontongju.auction.dto.response.AuctionResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.quota.ClientQuotaAlteration.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
public class AuctionRepositoryTest {

  @Autowired
  private AuctionRepository auctionRepository;

  @Autowired
  private AuctionProductRepository auctionProductRepository;

  @Autowired
  private BidInfoRepository bidInfoRepository;

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
  @Order(1)
  @DisplayName("경매 생성 - Entity를 저장했으므로 0이 아니어야 함")
  void createAuction() {
    Auction auction = Auction.builder()
        .title("제 19회 복순도가 경매대회")
        .description("복순복순 복순도가")
        .startDate(LocalDateTime.parse("2023-11-24T17:00:00"))
        .build();

    auctionRepository.save(auction);

    assertNotEquals(0, auctionRepository.count());
  }

  @Test
  @Order(2)
  @DisplayName("경매 조회 - 기존에 생성한 제목과 같아야 함")
  void readAuction() {
    Auction auction = auctionRepository.findById(initAuction.getAuctionId()).orElseThrow(
        AuctionNotFoundException::new);

    assertEquals(auction.getTitle(), "제 20회 복순도가 경매대회");
  }

  @Test
  @Order(3)
  @DisplayName("경매 수정 - 기존 제목과 달라야 함")
  void updateAuction() {
    auctionRepository.save(initAuction.toBuilder().title("복순도가 경매 취소").build());

    assertNotEquals(initAuction.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(initAuction.getTitle(), "복순도가 경매 취소");
  }

  @Test
  @Order(4)
  @DisplayName("경매 삭제 - 삭제한 경매 정보가 없어야 함")
  void deleteAuction() {
    auctionRepository.deleteById(initAuction.getAuctionId());

    assertFalse(auctionRepository.existsById(initAuction.getAuctionId()));
  }

  @Test
  @Order(5)
  @DisplayName("등록 가능한 경매 조회 - 3개 경매 상품 중 1개만 승인 상태")
  void getRegistrableAuction() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    SellerAuctionResponseDto responseDto = auctionRepository.findRegistrableAuction()
        .orElseThrow(AuctionNotFoundException::new);

    assertEquals(responseDto.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(responseDto.getCurrentParticipants(), 1);
  }

  @Test
  @Order(6)
  @DisplayName("출품 내역 조회 - 복순도가만 경매 완료하여 낙찰정보가 있는 상태")
  void getAuctionEntries() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);
    AuctionProduct auctionProduct = auctionProductRepository.findByName("복순도가").orElseThrow(
        EntityNotFoundException::new);

    initBidInfoList = initBidInfo(initAuction, auctionProduct);
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    Page<SellerAuctionEntriesResponseDto> response = auctionProductRepository.findAuctionProductBySellerId(
        1L, PageRequest.of(0, 10)).map(SellerAuctionEntriesResponseDto::new);

    response.forEach(dto -> {
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
  @Order(7)
  @DisplayName("경매 목록 조회 - 경매 상태에 따른 현황(승인, 대기, 거절, 참여 수)")
  void getAdminAuction() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    Auction auction2 = initAuction("제 19회 복순도가 경매대회", AuctionStatusEnum.AFTER);
    auctionRepository.save(auction2);

    List<AuctionProduct> initProductList2 = initAuctionProduct(auction2);
    auctionProductRepository.saveAll(initProductList2);

    initBidInfoList = initBidInfo(initAuction, initProductList.get(0));
    List<BidInfo> initBidInfoList2 = initBidInfo(auction2, initProductList2.get(0));

    bidInfoRepository.saveAll(initBidInfoList);
    bidInfoRepository.saveAll(initBidInfoList2);

    entityManager.flush();
    entityManager.clear();

    Page<AdminAuctionResponseDto> response = auctionRepository.findAll(PageRequest.of(0, 10))
        .map(AdminAuctionResponseDto::new);

    response.forEach(adminAuctionResponseDto -> {
          if (adminAuctionResponseDto.getStatus().equals(AuctionStatusEnum.AFTER)) {
            assertEquals(adminAuctionResponseDto.getParticipation(), 1);
            assertNull(adminAuctionResponseDto.getAllow());
            assertNull(adminAuctionResponseDto.getDeny());
            assertNull(adminAuctionResponseDto.getWait());
          } else {
            assertEquals(adminAuctionResponseDto.getAllow(), 1);
            assertEquals(adminAuctionResponseDto.getDeny(), 1);
            assertEquals(adminAuctionResponseDto.getWait(), 1);
            assertNull(adminAuctionResponseDto.getParticipation());
          }
        }
    );
  }

  @Test
  @Order(8)
  @DisplayName("특정 경매 상세 조회")
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

    initAuction = auctionRepository.findById(initAuction.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);
    auction2 = auctionRepository.findById(auction2.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);

    List<AuctionProductResponseDto> productResponseDtoList =
        Objects.isNull(initAuction.getAuctionProductList()) ? null :
            initAuction.getAuctionProductList().stream()
                .map(AuctionProductResponseDto::new).collect(
                    Collectors.toList());

    AuctionDetailResponseDto auctionBeforeResponse = AuctionDetailResponseDto.builder()
        .auction(
            Optional.of(initAuction).map(AuctionResponseDto::new)
                .orElseThrow(AuctionNotFoundException::new)
        )
        .productList(productResponseDtoList)
        .build();

    List<AuctionProductBidResponseDto> auctionProductBidResponseDtoList =
        Objects.isNull(auction2.getAuctionProductList()) ? null :
            auction2.getAuctionProductList().stream()
                .map(AuctionProductBidResponseDto::new).collect(
                    Collectors.toList());

    AuctionDetailResponseDto auctionAfterResponse = AuctionDetailResponseDto.builder()
        .auction(
            Optional.of(auction2).map(AuctionResponseDto::new)
                .orElseThrow(AuctionNotFoundException::new)
        )
        .productList(auctionProductBidResponseDtoList)
        .build();

    assertEquals(auctionBeforeResponse.getProductList().get(0).getBusinessmanName(), "김덤보");

    AuctionProductBidResponseDto bid = (AuctionProductBidResponseDto) auctionAfterResponse.getProductList()
        .get(0);

    assertEquals(bid.getLastBidPrice(), 12000);
  }

  @Test
  @Order(9)
  @DisplayName("이번 주 금요일 열리는 경매 조회")
  void getThisAuction() {
    Auction auction = auctionRepository.findThisAuction()
        .orElseThrow(AuctionNotFoundException::new);

    assertEquals(auction.getTitle(), "제 20회 복순도가 경매대회");
  }

  private Auction initAuction(String title, AuctionStatusEnum auctionStatusEnum) {
    return Auction.builder()
        .title(title)
        .description("복순도가 누가 가져갈 것 인가")
        .startDate(LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY)), LocalTime.of(17, 0)))
        .status(auctionStatusEnum)
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
