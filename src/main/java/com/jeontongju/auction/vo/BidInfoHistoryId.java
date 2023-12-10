package com.jeontongju.auction.vo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTyped(DynamoDBAttributeType.M)
public class BidInfoHistoryId implements Serializable {
  private static final long serialVersionUID = 1L;

  private String auctionProductId;
  private Long bidPrice;

  @DynamoDBHashKey(attributeName = "auction_product_id")
  public String getAuctionProductId() {
    return auctionProductId;
  }

  public void setAuctionProductId(String auctionProductId) {
    this.auctionProductId = auctionProductId;
  }

  @DynamoDBRangeKey(attributeName = "bid_price")
  public Long getBidPrice() {
    return bidPrice;
  }

  public void setBidPrice(Long bidPrice) {
    this.bidPrice = bidPrice;
  }

  public static BidInfoHistoryId of(String auctionProductId, Long bidPrice) {
    return BidInfoHistoryId.builder()
        .auctionProductId(auctionProductId)
        .bidPrice(bidPrice)
        .build();
  }
}
