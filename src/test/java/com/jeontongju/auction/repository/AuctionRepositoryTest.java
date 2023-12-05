package com.jeontongju.auction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
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
    initAuction = initAuction();
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
  @DisplayName("셀러 - 등록 가능한 경매 조회")
  void getRegistrableAuction() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    SellerAuctionResponseDto responseDto = auctionRepository.findRegistrableAuction()
        .orElseThrow(AuctionNotFoundException::new);

    assertEquals(responseDto.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(responseDto.getCurrentParticipants(), 3);
  }

  @Test
  @Order(6)
  @DisplayName("셀러 - 출품 내역 조회")
  void getAuctionEntries() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);
    AuctionProduct auctionProduct = auctionProductRepository.findByName("복순도가").orElseThrow(
        EntityNotFoundException::new);

    initBidInfoList = initBidInfo();
    initBidInfoList = initBidInfoList.stream()
        .map(bidInfo -> bidInfo.toBuilder().auctionProduct(auctionProduct).build())
        .collect(Collectors.toList());
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
  @DisplayName("관리자 - 경매 목록 조회")
  public void getAdminAuction() {
    initProductList = initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    Auction auction2 = Auction.builder()
        .title("제 19회 복순도가 경매대회")
        .description("복순도가 누가 가져갈 것 인가")
        .startDate(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)))
        .status(AuctionStatusEnum.AFTER)
        .build();

    auctionRepository.save(auction2);

    List<AuctionProduct> initProductList2 = initAuctionProduct(auction2);
    auctionProductRepository.saveAll(initProductList2);

    entityManager.flush();
    entityManager.clear();

    Page<AdminAuctionResponseDto> response = auctionRepository.findAll(PageRequest.of(0, 10))
        .map(AdminAuctionResponseDto::new);

    response.forEach(adminAuctionResponseDto -> {
          if (adminAuctionResponseDto.getTitle().contains("19회")) {
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
