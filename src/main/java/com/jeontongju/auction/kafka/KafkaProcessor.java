package com.jeontongju.auction.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProcessor<T> {
  private final KafkaTemplate<String, T> kafkaTemplate;

  public void send(String topic, T data) {
    kafkaTemplate.send(topic, data);
  }
}
