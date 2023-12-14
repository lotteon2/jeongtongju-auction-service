package com.jeontongju.auction.domain;

import com.jeontongju.auction.domain.common.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bid_info", indexes = @Index(name = "idx_consumer_id", columnList = "consumer_id"))
public class BidInfo extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bidInfoId;

  @JoinColumn(name = "auction_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Auction auction;

  @JoinColumn(name = "auction_product_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private AuctionProduct auctionProduct;

  @NotNull
  @Column(name = "consumer_id")
  private Long consumerId;

  @NotNull
  private Long bidPrice;

  @NotNull
  @Builder.Default
  private Boolean isBid = false;
}
