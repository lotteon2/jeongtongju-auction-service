package com.jeontongju.auction.domain;

import com.jeontongju.auction.domain.common.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidInfo extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bidInfoId;

  @JoinColumn(name = "auction_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Auction auction;

  @JoinColumn(name = "auction_product_id")
  @OneToOne(fetch = FetchType.LAZY)
  private AuctionProduct auctionProduct;

  @NotNull
  private Long consumerId;

  @NotNull
  private Long totalBid;

  @NotNull
  private Long bidPrice;

  @NotNull
  private Long lastBidPrice;

  @NotNull
  private Boolean isBid;
}
