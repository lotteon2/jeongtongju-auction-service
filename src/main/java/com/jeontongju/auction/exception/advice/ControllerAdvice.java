package com.jeontongju.auction.exception.advice;

import com.jeontongju.auction.exception.DuplicateSellerRegisterProductException;
import com.jeontongju.auction.exception.EmptyAuctionProductException;
import com.jeontongju.auction.exception.InvalidAuctionStatusException;
import com.jeontongju.auction.exception.InvalidBidPriceException;
import com.jeontongju.auction.exception.InvalidConsumerCreditException;
import com.jeontongju.auction.exception.OverParticipationException;
import com.jeontongju.auction.exception.SameBidPriceException;
import com.jeontongju.auction.exception.SameWeekOfAuctionException;
import com.jeontongju.auction.exception.common.EntityNotFoundException;
import io.github.bitbox.bitbox.dto.ResponseFormat;
import io.github.bitbox.bitbox.enums.FailureTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice extends ResponseEntityExceptionHandler {

  private static final String UNIQUE_CONSTRAINT_EXCEPTION_MESSAGE = "유니크 제약조건 오류 ";
  private static final String DUPLICATE_KEY_EXCEPTION_MESSAGE = "중복 키 오류 ";
  private static final String SOCKET_TRANSFER_EXCEPTION_MESSAGE = "소켓 통신 중 오류";

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {

    return ResponseEntity
        .status(status.value())
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(
                    e.getBindingResult().getFieldError() == null
                        ? e.getMessage()
                        : e.getBindingResult().getFieldError().getDefaultMessage())
                .build()
        );
  }

  @MessageExceptionHandler
  public ResponseEntity<ResponseFormat<Void>> handleMessageException(Exception e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(SOCKET_TRANSFER_EXCEPTION_MESSAGE)
                .build()
        );
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<ResponseFormat<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity
        .status(status.value())
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(DUPLICATE_KEY_EXCEPTION_MESSAGE)
                .build()
        );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ResponseFormat<Void>> handleConstraintViolationException(
      DataIntegrityViolationException e) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity
        .status(status.value())
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(UNIQUE_CONSTRAINT_EXCEPTION_MESSAGE)
                .build()
        );
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ResponseFormat<Void>> handleEntityNotFoundException(
      EntityNotFoundException e) {
    HttpStatus status = e.getStatus();
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }

  @ExceptionHandler(OverParticipationException.class)
  public ResponseEntity<ResponseFormat<Void>> handleOverParticipantionExcepion(
      OverParticipationException e) {
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .failure(FailureTypeEnum.OVER_PARTICIPATION)
                .build()
        );
  }

  @ExceptionHandler(InvalidAuctionStatusException.class)
  public ResponseEntity<ResponseFormat<Void>> handleAuctionInvalidStatusException(
      InvalidAuctionStatusException e
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }

  @ExceptionHandler(InvalidConsumerCreditException.class)
  public ResponseEntity<ResponseFormat<Void>> handleConsumerInvalidCreditException(
      InvalidConsumerCreditException e
  ) {
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity
        .ok()
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .failure(FailureTypeEnum.INVALID_CONSUMER_CREDIT)
                .build()
        );
  }

  @ExceptionHandler(SameBidPriceException.class)
  public ResponseEntity<ResponseFormat<Void>> handleSameBidPriceException(
      SameBidPriceException e
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }

  @ExceptionHandler(SameWeekOfAuctionException.class)
  public ResponseEntity<ResponseFormat<Void>> handleSameWeekOfAuctionException(
      SameWeekOfAuctionException e
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }

  @ExceptionHandler(DuplicateSellerRegisterProductException.class)
  public ResponseEntity<ResponseFormat<Void>> handleAlreadyRegisteredProductException(
      DuplicateSellerRegisterProductException e
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }

  @ExceptionHandler(EmptyAuctionProductException.class)
  public ResponseEntity<ResponseFormat<Void>> handleEmptyAuctionProductException(
      EmptyAuctionProductException e
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }

  @ExceptionHandler(InvalidBidPriceException.class)
  public ResponseEntity<ResponseFormat<Void>> handleInvalidBidPriceException(
      InvalidBidPriceException e
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity
        .status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(e.getMessage())
                .build()
        );
  }
}
