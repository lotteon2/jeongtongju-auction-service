package com.jeontongju.auction.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
