package com.jeontongju.auction.service;

import com.jeontongju.auction.client.SellerServiceFeignClient;
import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.request.AuctionProductRegisterRequestDto;
import com.jeontongju.auction.dto.request.AuctionRegisterRequestDto;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductResponseDto;
import com.jeontongju.auction.dto.response.AuctionResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.dto.temp.SellerInfoForAuctionDto;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.exception.OverParticipationException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

  private final AuctionRepository auctionRepository;
  private final AuctionProductRepository auctionProductRepository;
  private final SellerServiceFeignClient sellerServiceFeignClient;
  private static final Long LIMIT_PARTICIPANTS = 5L;

  /**
   * 셀러 - 등록 가능한 경매 조회
   *
   * @return SellerAuctionResponseDto
   */
  public SellerAuctionResponseDto getRegistrableAuction() {
    return auctionRepository.findRegistrableAuction().orElseThrow(AuctionNotFoundException::new);
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
    return auctionRepository.findAll(pageable).map(AdminAuctionResponseDto::new);
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
    Auction auction = auctionRepository.findThisAuction()
        .orElseThrow(AuctionNotFoundException::new);
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

  @Transactional
  public void registerAuctionProduct(AuctionProductRegisterRequestDto request, Long sellerId) {
    Auction auction = auctionRepository.findById(request.getAuctionId())
        .orElseThrow(AuctionNotFoundException::new);

    long participants =
        auction.getAuctionProductList() == null ? 0L
            : auction.getAuctionProductList().stream()
                .filter(auctionProduct -> auctionProduct.getStatus().equals(
                    AuctionProductStatusEnum.ALLOW)).count();

    if (participants > LIMIT_PARTICIPANTS) {
      throw new OverParticipationException();
    }

    SellerInfoForAuctionDto sellerInfo = sellerServiceFeignClient.getSellerInfoForCreateAuctionProduct(
        sellerId).getData();

    auctionProductRepository.save(request.toEntity(auction, sellerInfo, sellerId));
  }

  @Transactional
  public void registerAuction(AuctionRegisterRequestDto request) {
    auctionRepository.save(request.toEntity());
  }

  @Transactional
  public void deleteAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);
    
    auctionRepository.save(auction.toBuilder().isDeleted(true).build());
  }
}
