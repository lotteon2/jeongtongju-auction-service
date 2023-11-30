package com.jeontongju.auction.domain;

import com.jeontongju.auction.domain.common.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionProduct extends BaseEntity {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "auction_product_id", columnDefinition = "CHAR(36)")
  private String auctionProductId;

  @JoinColumn(name = "auction_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Auction auction;

  @NotNull
  private String name;

  @NotNull
  private Long startingPrice;

  @NotNull
  private String description;

  @NotNull
  private Long capacity;

  @NotNull
  private Long alcoholDegree;

  @NotNull
  private String thumbnailImageUrl;

  @NotNull
  private String status;

  @NotNull
  private Long sellerId;

  @NotNull
  private String storeImageUrl;

  @NotNull
  private String storeName;

  @NotNull
  private String storeEmail;

  @NotNull
  private String storePhoneNumber;

  @NotNull
  private String businessmanName;
}
