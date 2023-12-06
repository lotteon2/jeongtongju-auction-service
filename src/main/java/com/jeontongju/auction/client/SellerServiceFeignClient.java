package com.jeontongju.auction.client;

import com.jeontongju.auction.dto.temp.FeignFormat;
import com.jeontongju.auction.dto.temp.SellerInfoForAuctionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "seller-service")
public interface SellerServiceFeignClient {
  @GetMapping("/sellers/{sellerId}/auction")
  FeignFormat<SellerInfoForAuctionDto> getSellerInfoForCreateAuctionProduct(@PathVariable Long sellerId);
}
