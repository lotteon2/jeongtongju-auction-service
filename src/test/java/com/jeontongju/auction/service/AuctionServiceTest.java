package com.jeontongju.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.dto.request.AuctionModifyRequestDto;
import com.jeontongju.auction.dto.request.AuctionProductRegisterRequestDto;
import com.jeontongju.auction.dto.request.AuctionRegisterRequestDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.ConsumerAuctionBidResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.DuplicateSellerRegisterProductException;
import com.jeontongju.auction.exception.SameWeekOfAuctionException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import com.jeontongju.auction.util.InitData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
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
  @DisplayName("셀러 등록 가능한 경매 조회 - 3개 경매 상품 중 1개만 승인 상태")
  void getRegistrableAuction() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    SellerAuctionResponseDto registrableAuction = auctionService.getRegistrableAuction();

    assertEquals(registrableAuction.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(registrableAuction.getCurrentParticipants(), 1);
  }

  @Test
  @DisplayName("셀러 등록 가능한 경매 조회 - 경매 상품이 없을 경우")
  void getRegistrableAuctionWithOutProduct() {

    SellerAuctionResponseDto registrableAuction = auctionService.getRegistrableAuction();

    assertEquals(registrableAuction.getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(registrableAuction.getCurrentParticipants(), 0);
  }

  @Test
  @DisplayName("셀러 출품 내역 조회 - 복순도가만 경매 완료하여 낙찰정보가 있는 상태")
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

    Page<SellerAuctionEntriesResponseDto> auctionEntries = auctionService.getAuctionEntries(1L,
        PageRequest.of(0, 10));

    auctionEntries.forEach(dto -> {
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
  @DisplayName("관리자 특정 경매 상세 조회")
  void getAdminAuctionDetail() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    Auction auction2 = init.initAuction("제 19회 복순도가 경매대회", AuctionStatusEnum.AFTER);
    auctionRepository.save(auction2);

    List<AuctionProduct> initProductList2 = init.initAuctionProduct(auction2);
    auctionProductRepository.saveAll(initProductList2);

    initBidInfoList = init.initBidInfo(auction2, initProductList2.get(0), initProductList2.get(1));
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    AuctionDetailResponseDto auctionBeforeResponse = auctionService.getAdminAuctionDetail(
        initAuction.getAuctionId());

    AuctionDetailResponseDto auctionAfterResponse = auctionService.getAdminAuctionDetail(
        auction2.getAuctionId());

    assertEquals(auctionBeforeResponse.getProductList().get(0).getBusinessmanName(), "김덤보");

    List<AuctionProductBidResponseDto> bidList = (List<AuctionProductBidResponseDto>) auctionAfterResponse.getProductList();

    bidList.forEach(auctionProductBidResponseDto -> {
      if (auctionProductBidResponseDto.getProductName().equals("복순도가")) {
        assertNotNull(auctionProductBidResponseDto.getLastBidPrice());
      }
    });
  }

  @Test
  @DisplayName("소비자 이번 주 열리는 경매 상세 조회")
  void getThisAuctionDetail() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    entityManager.flush();
    entityManager.clear();

    AuctionDetailResponseDto thisAuctionDetail = auctionService.getThisAuctionDetail();

    assertEquals(thisAuctionDetail.getAuction().getTitle(), "제 20회 복순도가 경매대회");
    assertEquals(thisAuctionDetail.getProductList().get(0).getSellerName(), "덤보네");
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

  @Test
  @DisplayName("경매 물품 승인")
  void approveAuctionProduct() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    auctionService.approveAuctionProduct(initProductList.get(0).getAuctionProductId(),
        AuctionProductStatusEnum.ALLOW);
    auctionService.approveAuctionProduct(initProductList.get(1).getAuctionProductId(),
        AuctionProductStatusEnum.DENY);

    assertEquals(initProductList.get(0).getStatus(), AuctionProductStatusEnum.ALLOW);
    assertEquals(initProductList.get(1).getStatus(), AuctionProductStatusEnum.DENY);
  }

  @Test
  @DisplayName("소비자 입찰 내역 조회")
  void getConsumerBidInfo() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    initBidInfoList = init.initBidInfo(initAuction, initProductList.get(0), initProductList.get(1));
    bidInfoRepository.saveAll(initBidInfoList);

    entityManager.flush();
    entityManager.clear();

    Page<ConsumerAuctionBidResponseDto> consumerBidInfo = auctionService.getConsumerBidInfo(1L,
        PageRequest.of(0, 10));

    assertEquals(consumerBidInfo.getContent().size(), 2);
    assertEquals(consumerBidInfo.getContent().get(0).getAuctionId(), initAuction.getAuctionId());
  }


  @Test
  @DisplayName("같은 주차 경매 생성 불가")
  void sameWeekRegisterAuction() {
    AuctionRegisterRequestDto request = AuctionRegisterRequestDto.builder()
        .title("제 20회 복순도가 경매대회 2")
        .description("설명 설명")
        .startDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")))
        .build();

    assertThrows(SameWeekOfAuctionException.class, () -> auctionService.registerAuction(request));
  }

  @Test
  @DisplayName("이미 경매에 등록된 셀러 재등록 불가")
  void alreadyRegisteredProduct() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    entityManager.flush();
    entityManager.clear();

    AuctionProductRegisterRequestDto request = AuctionProductRegisterRequestDto.builder()
        .auctionId(initAuction.getAuctionId())
        .auctionProductName("복순2도가")
        .startingPrice(10000L)
        .thumbnailImageUrl("")
        .description("설명")
        .capacity(500L)
        .alcoholDegree(14.0)
        .build();

    assertThrows(DuplicateSellerRegisterProductException.class,
        () -> auctionService.registerAuctionProduct(request, 1L));

  }

  @Test
  @DisplayName("허용된 경매 상품 수")
  void getAllowProducts() {
    initProductList = init.initAuctionProduct(initAuction);
    auctionProductRepository.saveAll(initProductList);

    entityManager.flush();
    entityManager.clear();

    Long count = auctionService.getAllowProductCount();
    assertTrue(count != 0);
  }

}
