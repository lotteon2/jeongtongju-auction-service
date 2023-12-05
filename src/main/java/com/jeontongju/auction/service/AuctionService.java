package com.jeontongju.auction.service;

import com.jeontongju.auction.domain.Auction;
import com.jeontongju.auction.domain.AuctionProduct;
import com.jeontongju.auction.dto.response.AdminAuctionResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionEntriesResponseDto;
import com.jeontongju.auction.dto.response.SellerAuctionResponseDto;
import com.jeontongju.auction.exception.AuctionNotFoundException;
import com.jeontongju.auction.repository.AuctionProductRepository;
import com.jeontongju.auction.repository.AuctionRepository;
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
   * @return SellerAuctionResponseDto
   */
  public SellerAuctionResponseDto getRegistrableAuction() {
    return auctionRepository.findRegistrableAuction().orElseThrow(AuctionNotFoundException::new);
  }

  /**
   * 셀러 - 출품 내역 조회
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
   * @param pageable
   * @return Page<AdminAuctionResponseDto>
   */
  public Page<AdminAuctionResponseDto> getAdminAuction(Pageable pageable) {
    return auctionRepository.findAll(pageable).map(AdminAuctionResponseDto::new);
  }

}
