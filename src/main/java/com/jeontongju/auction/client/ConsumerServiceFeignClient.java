package com.jeontongju.auction.client;

import com.jeontongju.auction.dto.temp.ConsumerInfoDto;
import com.jeontongju.auction.dto.temp.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "consumer-service")
public interface ConsumerServiceFeignClient {
  @GetMapping("/consumers/{consumerId}/name-image")
  FeignFormat<ConsumerInfoDto> getConsumerInfo(@PathVariable Long consumerId);

}
