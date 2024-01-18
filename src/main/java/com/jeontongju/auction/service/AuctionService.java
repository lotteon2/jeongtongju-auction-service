package com.jeontongju.auction.service;

import com.jeontongju.auction.client.SellerServiceFeignClient;
import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.domain.BidInfo;
import com.jeontongju.auction.dto.request.AuctionModifyRequestDto;
import com.jeontongju.auction.dto.request.AuctionProductRegisterRequestDto;
import com.jeontongju.auction.dto.request.AuctionRegisterRequestDto;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductResponseDto;
import com.jeontongju.auction.dto.response.AuctionResponseDto;
import com.jeontongju.auction.dto.response.ConsumerAuctionBidResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.DuplicateSellerRegisterProductException;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.exception.AuctionProductNotFoundException;
import com.jeontongju.auction.exception.OverParticipationException;
import com.jeontongju.auction.exception.SameWeekOfAuctionException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import com.jeontongju.auction.repository.BidInfoRepository;
import io.github.bitbox.bitbox.dto.SellerInfoForAuctionDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

  private final AuctionRepository auctionRepository;
  private final AuctionProductRepository auctionProductRepository;
  private final BidInfoRepository bidInfoRepository;
  private final SellerServiceFeignClient sellerServiceFeignClient;
  private static final Long LIMIT_PARTICIPANTS = 20L;

  /**
   * 셀러 - 등록 가능한 경매 조회
   *
   * @return SellerAuctionResponseDto
   */
  public SellerAuctionResponseDto getRegistrableAuction() {
//    return auctionRepository.findRegistrableAuction().orElseThrow(AuctionNotFoundException::new);
    return auctionRepository.findRegistrableAuctionRecent().orElseThrow(AuctionNotFoundException::new);
  }

  /**
   * 셀러 - 출품 내역 조회
   *
   * @param sellerId
   * @param pageable
   * @return Page<SellerAuctionEntriesResponseDto>
   */
  public Page<SellerAuctionEntriesResponseDto> getAuctionEntries(Long sellerId, Pageable pageable) {
    return auctionProductRepository.findAuctionProductBySellerId(
        sellerId, pageable).map(SellerAuctionEntriesResponseDto::new);
  }

  /**
   * 관리자 - 경매 목록 조회
   *
   * @param pageable
   * @return Page<AdminAuctionResponseDto>
   */
  public Page<AdminAuctionResponseDto> getAdminAuction(Pageable pageable) {
    return auctionRepository.findAllByIsDeletedIsFalse(pageable).map(AdminAuctionResponseDto::new);
  }

  /**
   * 관리자 - 특정 경매 상세 조회
   *
   * @param auctionId
   * @return AuctionDetailResponseDto
   */
  public AuctionDetailResponseDto getAdminAuctionDetail(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    if (auction.getStatus().equals(AuctionStatusEnum.AFTER)) {
      // AuctionProductBidResponseDto
      List<AuctionProductBidResponseDto> auctionProductBidResponseDtoList =
          Objects.isNull(auction.getAuctionProductList()) ? null :
              auction.getAuctionProductList()
                  .stream()
                  .map(AuctionProductBidResponseDto::new)
                  .collect(Collectors.toList());

      return AuctionDetailResponseDto.builder()
          .auction(
              Optional.of(auction).map(AuctionResponseDto::new)
                  .orElseThrow(AuctionNotFoundException::new)
          )
          .productList(auctionProductBidResponseDtoList)
          .build();
    } else {
      // AuctionProductResponseDto
      List<AuctionProductResponseDto> productResponseDtoList =
          Objects.isNull(auction.getAuctionProductList()) ? null :
              auction.getAuctionProductList()
                  .stream()
                  .map(AuctionProductResponseDto::new)
                  .collect(Collectors.toList());

      return AuctionDetailResponseDto.builder()
          .auction(
              Optional.of(auction).map(AuctionResponseDto::new)
                  .orElseThrow(AuctionNotFoundException::new)
          )
          .productList(productResponseDtoList)
          .build();
    }
  }

  /**
   * 소비자 - 이번 주 경매 조회 상세
   *
   * @return AuctionDetailResponseDto
   */
  public AuctionDetailResponseDto getThisAuctionDetail() {
//    Auction auction = auctionRepository.findThisAuction()
//    .orElseThrow(AuctionNotFoundException::new);

    Auction auction = auctionRepository.findThisAuctionRecent()
        .orElseThrow(AuctionNotFoundException::new);

    return AuctionDetailResponseDto.of(auction);
  }

  @Transactional
  public void registerAuctionProduct(AuctionProductRegisterRequestDto request, Long sellerId) {
    Auction auction = auctionRepository.findById(request.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);

    long participants =
        auction.getAuctionProductList() == null ? 0L
            : auction.getAuctionProductList()
                .stream()
                .filter(
                    auctionProduct -> {
                      if (auctionProduct.getSellerId().equals(sellerId) &&
                          !auctionProduct.getStatus().equals(AuctionProductStatusEnum.DENY)) {
                        throw new DuplicateSellerRegisterProductException();
                      }

                      return auctionProduct.getStatus().equals(AuctionProductStatusEnum.ALLOW);
                    }
                ).count();

    if (participants >= LIMIT_PARTICIPANTS) {
      throw new OverParticipationException();
    }

    SellerInfoForAuctionDto sellerInfo = sellerServiceFeignClient.getSellerInfoForCreateAuctionProduct(
        sellerId).getData();

    auctionProductRepository.save(request.toEntity(auction, sellerInfo, sellerId));
  }

  @Transactional
  public void registerAuction(AuctionRegisterRequestDto request) {
    Long count = auctionRepository.findDateOfWeek(
        LocalDate.parse(request.getStartDate(), DateTimeFormatter.ofPattern("yyyy.M.d.")));

    if (count > 0) {
      throw new SameWeekOfAuctionException();
    }

    auctionRepository.save(request.toEntity());
  }

  @Transactional
  public void deleteAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    auctionRepository.save(auction.toBuilder().isDeleted(true).build());
  }

  @Transactional
  public void modifyAuction(AuctionModifyRequestDto request, String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    auctionRepository.save(request.toEntity(auction));
  }

  @Transactional
  public void approveAuctionProduct(String auctionProductId,
      AuctionProductStatusEnum confirmStatus) {
    AuctionProduct auctionProduct = auctionProductRepository.findById(auctionProductId).orElseThrow(
        AuctionProductNotFoundException::new);

    auctionProductRepository.save(auctionProduct.toBuilder().status(confirmStatus).build());
  }

  public Page<ConsumerAuctionBidResponseDto> getConsumerBidInfo(Long consumerId,
      Pageable pageable) {
    Map<String, Long> lastBidMap = bidInfoRepository.findAllByIsBidTrue().stream()
        .collect(Collectors.toMap(bidInfo -> bidInfo.getAuctionProduct().getAuctionProductId(),
            BidInfo::getBidPrice));

    List<ConsumerAuctionBidResponseDto> result = bidInfoRepository.findByConsumerId(consumerId)
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
        .peek(dto -> dto.initLastBidPrice(lastBidMap.get(dto.getProductId())))
        .collect(Collectors.toList()
        );

    return toPage(result, pageable);
  }

  public Long getAllowProductCount() {
    return auctionRepository.countByAuctionProductIsWait();
  }


  <T> Page<T> toPage(List<T> list, Pageable pageable) {
    List<T> pageList = list.stream()
        .skip((long) pageable.getPageNumber() * pageable.getPageSize())
        .limit(pageable.getPageSize())
        .collect(Collectors.toList());

    return new PageImpl<>(pageList, pageable, pageList.size());
  }
}
