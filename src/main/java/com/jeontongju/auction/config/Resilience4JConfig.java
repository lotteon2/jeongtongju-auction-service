package com.jeontongju.auction.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4JConfig {

  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

    CircuitBreakerConfig circuitBreakerConfig =
        CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .slowCallRateThreshold(80f)
            .failureRateThreshold(80f)
            .minimumNumberOfCalls(100)
            .slidingWindowSize(100)
            .permittedNumberOfCallsInHalfOpenState(100)
            .maxWaitDurationInHalfOpenState(Duration.ofMillis(1000))
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .automaticTransitionFromOpenToHalfOpenEnabled(false)
            .ignoreExceptions(ClassCastException.class)
            .build();

    TimeLimiterConfig timeLimiterConfig =
        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build();

    return factory ->
        factory.configureDefault(
            id ->
                new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(timeLimiterConfig)
                    .circuitBreakerConfig(circuitBreakerConfig)
                    .build());
  }
}