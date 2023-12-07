package com.jeontongju.auction.domain;

import com.jeontongju.auction.domain.common.BaseEntity;
import com.jeontongju.auction.enums.AuctionStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auction")
public class Auction extends BaseEntity {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "auction_id", columnDefinition = "CHAR(36)")
  private String auctionId;

  @NotNull
  private String title;

  @NotNull
  private String description;

  @NotNull
  @Column(name = "start_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime startDate;

  @Column(name = "end_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime endDate;

  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private AuctionStatusEnum status = AuctionStatusEnum.BEFORE;

  @NotNull
  @Builder.Default
  private Boolean isDeleted = false;

  @OneToMany(mappedBy = "auction")
  List<AuctionProduct> auctionProductList;

  @OneToMany(mappedBy = "auction")
  List<BidInfo> bidInfoList;
}
