package com.jeontongju.auction.dto.kafka;

import com.jeontongju.auction.dto.request.ChatMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaChatMessageDto {
  private String auctionId;
  private Long memberId;
  private String memberNickname;
  private String memberProfileImage;
  private String message;

  public static KafkaChatMessageDto toKafkaChatMessageDto(ChatMessageDto messageDto, String auctionId) {
    return KafkaChatMessageDto.builder()
        .auctionId(auctionId)
        .memberId(messageDto.getMemberId())
        .memberNickname(messageDto.getMemberNickname())
        .memberProfileImage(messageDto.getMemberProfileImage())
        .message(messageDto.getMessage())
        .build();
  }
}
