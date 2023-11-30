package com.jeontongju.auction.domain;

import com.jeontongju.auction.domain.common.BaseEntity;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
  private String status;

  @NotNull
  private Boolean isDeleted;

  @OneToMany(mappedBy = "auction")
  List<AuctionProduct> auctionProductList;

  @OneToMany(mappedBy = "auction")
  List<BidInfo> bidInfoList;
}
