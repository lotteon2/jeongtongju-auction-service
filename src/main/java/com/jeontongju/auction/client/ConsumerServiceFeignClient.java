package com.jeontongju.auction.client;

import io.github.bitbox.bitbox.dto.ConsumerInfoDto;
import io.github.bitbox.bitbox.dto.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "consumer-service")
public interface ConsumerServiceFeignClient {
  @GetMapping("/consumers/{consumerId}/auction")
  FeignFormat<ConsumerInfoDto> getConsumerInfo(@PathVariable Long consumerId);
  @PutMapping("/consumers/{consumerId}/credit/{deductionCredit}")
  FeignFormat<Boolean> deductCredit(@PathVariable Long consumerId, @PathVariable Long deductionCredit);
}
