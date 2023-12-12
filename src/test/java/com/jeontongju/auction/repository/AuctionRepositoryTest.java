package com.jeontongju.auction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.domain.BidInfoHistory;
import com.jeontongju.auction.dto.request.AuctionProductRegisterRequestDto;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.ConsumerAuctionBidResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.dto.temp.SellerInfoForAuctionDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.exception.AuctionProductNotFoundException;
import com.jeontongju.auction.exception.SameWeekOfAuctionException;
import com.jeontongju.auction.util.InitData;
import com.jeontongju.auction.vo.BidInfoHistoryId;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
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
  private BidInfoHistoryRepository bidInfoHistoryRepository;

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
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    SellerAuctionResponseDto responseDto = auctionRepository.findRegistrableAuction()
        .orElseThrow(AuctionNotFoundException::new);

    assertEquals(responseDto.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(responseDto.getCurrentParticipants(), 1);
  }

  @Test
  @Order(6)
  @DisplayName("출품 내역 조회 - 복순도가, 안동소주만 경매 완료하여 낙찰정보가 있는 상태")
  void getAuctionEntries() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    AuctionProduct auctionProduct = auctionProductRepository.findByName("복순도가").orElseThrow(
        EntityNotFoundException::new);
    AuctionProduct auctionProduct2 = auctionProductRepository.findByName("안동소주").orElseThrow(
        EntityNotFoundException::new);

    initBidInfoList = init.initBidInfo(initAuction, auctionProduct, auctionProduct2);
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    Page<SellerAuctionEntriesResponseDto> response = auctionProductRepository.findAuctionProductBySellerId(
        1L, PageRequest.of(0, 10)).map(SellerAuctionEntriesResponseDto::new);

    response.forEach(dto -> {
      if (dto.getAuctionProductName().equals("복순도가")) {
        assertEquals(dto.getLastBidPrice(), 12000);
        assertEquals(dto.getTotalBid(), 3);
      } else if (dto.getAuctionProductName().equals("안동소주")) {
        assertEquals(dto.getLastBidPrice(), 15000);
        assertEquals(dto.getTotalBid(), 2);
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
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    Auction auction2 = init.initAuction("제 19회 복순도가 경매대회", AuctionStatusEnum.AFTER);
    auctionRepository.save(auction2);

    List<AuctionProduct> initProductList2 = init.initAuctionProduct(auction2);
    auctionProductRepository.saveAll(initProductList2);

    initBidInfoList = init.initBidInfo(initAuction, initProductList.get(0), initProductList.get(1));
    List<BidInfo> initBidInfoList2 = init.initBidInfo(auction2, initProductList2.get(0),
        initProductList2.get(1));

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
    // 경매일정이 진행 전인 beforeAuction 추가
    Auction beforeAuction = init.initAuction("제 20회 복순도가 경매대회", AuctionStatusEnum.BEFORE);
    auctionRepository.save(beforeAuction);

    // initAuction에 출품한 상품 리스트를 추가 (복순도가, 안동소주, 막걸리나)
    initProductList = init.initAuctionProduct(beforeAuction);
    auctionProductRepository.saveAll(initProductList);

    // 경매일정이 완료된 경매 afterAuction 추가
    Auction afterAuction = init.initAuction("제 19회 복순도가 경매대회", AuctionStatusEnum.AFTER);
    auctionRepository.save(afterAuction);

    // afterAuction에 출품한 상품 리스트 추가 (복순도가, 안동소주, 막걸리나)
    List<AuctionProduct> initProductList2 = init.initAuctionProduct(afterAuction);
    auctionProductRepository.saveAll(initProductList2);

    // afterAuction에 출품한 상품에 입찰 정보 추가
    initBidInfoList = init.initBidInfo(afterAuction, initProductList2.get(0), initProductList2.get(1));
    bidInfoRepository.saveAll(initBidInfoList);

    // 영속성 초기화
    entityManager.flush();
    entityManager.clear();

    // 이후 양방향 연관을 갖는 영속성으로 다시 생성
    beforeAuction = auctionRepository.findById(beforeAuction.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);
    afterAuction = auctionRepository.findById(afterAuction.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);
    initBidInfoList = bidInfoRepository.findAll();

    AuctionDetailResponseDto auctionBeforeResponse = AuctionDetailResponseDto.of(beforeAuction);
    AuctionDetailResponseDto auctionAfterResponse = AuctionDetailResponseDto.of(afterAuction);

    // response dto의 productList가 있는지 확인
    assertEquals(auctionBeforeResponse.getProductList().get(0).getBusinessmanName(), "김덤보");

    // 경매가 완료된 이후의 결과는 낙찰 정보까지 찾아볼 수 있도록 type casting
    List<AuctionProductBidResponseDto> bidList = (List<AuctionProductBidResponseDto>) auctionAfterResponse.getProductList();

    // 경매 낙찰 정보가 있는지 확인
    bidList.forEach(auctionProductBidResponseDto -> {
      if (auctionProductBidResponseDto.getProductName().equals("복순도가")) {
        assertNotNull(auctionProductBidResponseDto.getLastBidPrice());
      }
    });
  }

  @Test
  @Order(9)
  @DisplayName("이번 주 금요일 열리는 경매 조회")
  void getThisAuction() {
    Auction auction = auctionRepository.findThisAuction()
        .orElseThrow(AuctionNotFoundException::new);

    assertEquals(auction.getTitle(), "제 20회 복순도가 경매대회");
  }

  @Test
  @Order(10)
  @DisplayName("경매 물품 등록 성공")
  void registAuctionProduct() {
    AuctionProductRegisterRequestDto request = AuctionProductRegisterRequestDto.builder()
        .auctionId(initAuction.getAuctionId())
        .auctionProductName("복순복순복순도가")
        .startingPrice(10000L)
        .thumbnailImageUrl("thumbnail_img")
        .description("복순도가 맛있다")
        .capacity(500L)
        .alcoholDegree(17.0)
        .build();

    SellerInfoForAuctionDto sellerInfo = SellerInfoForAuctionDto.builder()
        .storeName("덤보네")
        .storeImageUrl("store_img")
        .storeEmail("email@gmail.com")
        .storePhoneNumber("010-0101-0101")
        .businessmanName("김덤보")
        .build();

    auctionProductRepository.save(request.toEntity(initAuction, sellerInfo, 1L));
    AuctionProduct auctionProduct = auctionProductRepository.findByName("복순복순복순도가")
        .orElseThrow(AuctionProductNotFoundException::new);

    assertEquals(auctionProduct.getDescription(), "복순도가 맛있다");
  }

  @Test
  @Order(11)
  @DisplayName("소비자의 입찰 목록 조회")
  void getConsumersBidInfo() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    initBidInfoList = init.initBidInfo(initAuction, initProductList.get(0), initProductList.get(1));
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    Map<String, Long> lastBidMap = bidInfoRepository.findAllByIsBidTrue().stream()
        .collect(Collectors.toMap(bidInfo -> bidInfo.getAuctionProduct().getName(),
            BidInfo::getBidPrice));

    List<ConsumerAuctionBidResponseDto> result = bidInfoRepository.findByConsumerId(1L)
        .stream()
        .collect(
            Collectors.toMap(
                BidInfo::getAuctionProduct,
                Function.identity(),
                BinaryOperator.maxBy(Comparator.comparing(BidInfo::getBidPrice))
            )
        )
        .values()
        .stream()
        .map(ConsumerAuctionBidResponseDto::new)
        .peek(dto -> dto.initLastBidPrice(lastBidMap.get(dto.getProductName())))
        .collect(Collectors.toList()
        );

    result.forEach(response -> {
      long lastBidPrice = 15000;
      if (response.getProductName().equals("복순도가"))
        lastBidPrice = 12000;

      assertEquals(response.getLastBidPrice(), lastBidPrice);
    });
  }

  @Test
  @Order(12)
  @DisplayName("입찰 내역 저장")
  void insertBidInfoHistory() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    BidInfoHistoryId key = BidInfoHistoryId.builder()
        .auctionProductId(initProductList.get(0).getAuctionProductId())
        .bidPrice(20000L)
        .build();

    BidInfoHistory bidInfoHistory = BidInfoHistory.builder()
        .bidInfoHistoryId(key)
        .auctionId(initAuction.getAuctionId())
        .consumerId(1L)
        .build();

    bidInfoHistoryRepository.save(bidInfoHistory);
  }

  @Test
  @Order(12)
  @DisplayName("입찰 내역 조회")
  void getBidInfoHistory() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    BidInfoHistoryId key = BidInfoHistoryId.builder()
        .auctionProductId(initProductList.get(0).getAuctionProductId())
        .bidPrice(20000L)
        .build();

    BidInfoHistoryId key2 = BidInfoHistoryId.builder()
        .auctionProductId(initProductList.get(0).getAuctionProductId())
        .bidPrice(30000L)
        .build();

    List<BidInfoHistory> list = new ArrayList<>();

    BidInfoHistory bidInfoHistory1 = BidInfoHistory.builder()
        .bidInfoHistoryId(key)
        .auctionId(initAuction.getAuctionId())
        .consumerId(1L)
        .build();

    BidInfoHistory bidInfoHistory2 = BidInfoHistory.builder()
        .bidInfoHistoryId(key2)
        .auctionId(initAuction.getAuctionId())
        .consumerId(1L)
        .build();

    list.add(bidInfoHistory1);
    list.add(bidInfoHistory2);

    bidInfoHistoryRepository.saveAll(list);
    List<BidInfoHistory> result = bidInfoHistoryRepository.findByAuctionProductId(initProductList.get(0).getAuctionProductId());

    assertEquals(result.get(0).getBidPrice(), 20000L);
    assertEquals(result.get(1).getBidPrice(), 30000L);
  }
}
