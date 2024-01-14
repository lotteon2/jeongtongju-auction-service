package com.jeontongju.auction.domain;

import com.jeontongju.auction.domain.common.BaseEntity;
import com.jeontongju.auction.enums.AuctionProductStatusEnum;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auction_product", indexes = @Index(name = "idx_seller_id", columnList = "seller_id"))
public class AuctionProduct extends BaseEntity {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "auction_product_id", columnDefinition = "CHAR(36)")
  private String auctionProductId;

  @JoinColumn(name = "auction_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private Auction auction;

  @OneToMany(mappedBy = "auctionProduct")
  private List<BidInfo> bidInfoList;

  @NotNull
  private String name;

  @NotNull
  private Long startingPrice;

  @NotNull
  private String description;

  @NotNull
  private Long capacity;

  @NotNull
  private Double alcoholDegree;

  @NotNull
  private String thumbnailImageUrl;

  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private AuctionProductStatusEnum status = AuctionProductStatusEnum.WAIT;

  @NotNull
  @Column(name = "seller_id")
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
