package com.jeontongju.auction.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.jeontongju.auction.vo.BidInfoHistoryId;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "bid_info_history")
public class BidInfoHistory {

  @Id
  private BidInfoHistoryId bidInfoHistoryId;

  @DynamoDBHashKey(attributeName = "bid_info_history_id")
  public String getAuctionProductId() {
    return bidInfoHistoryId != null ? bidInfoHistoryId.getAuctionProductId() : null;
  }

  public void setAuctionProductId(String auctionProductId) {
    if (bidInfoHistoryId == null) {
      bidInfoHistoryId = new BidInfoHistoryId();
    }
    bidInfoHistoryId.setAuctionProductId(auctionProductId);
  }

  @DynamoDBRangeKey(attributeName = "bid_price")
  public Long getBidPrice() {
    return bidInfoHistoryId != null ? bidInfoHistoryId.getBidPrice() : null;
  }

  public void setBidPrice(Long bidPrice) {
    if (bidInfoHistoryId == null) {
      bidInfoHistoryId = new BidInfoHistoryId();
    }
    bidInfoHistoryId.setBidPrice(bidPrice);
  }

  @DynamoDBAttribute(attributeName = "consumer_id")
  private Long consumerId;

  @DynamoDBAttribute(attributeName = "auction_id")
  private String auctionId;


}
