package com.jeontongju.auction.service;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.AuctionDetailResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductBidResponseDto;
import com.jeontongju.auction.dto.response.AuctionProductResponseDto;
import com.jeontongju.auction.dto.response.AuctionResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import com.jeontongju.auction.exception.AuctionNotFoundException;
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

@Service
@RequiredArgsConstructor
public class AuctionService {

  private final AuctionRepository auctionRepository;
  private final AuctionProductRepository auctionProductRepository;

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

}
