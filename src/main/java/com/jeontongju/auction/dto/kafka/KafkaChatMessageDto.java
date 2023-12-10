package com.jeontongju.auction.dto.kafka;

import com.jeontongju.auction.dto.redis.MemberDto;
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

  public static KafkaChatMessageDto toKafkaChatMessageDto(ChatMessageDto messageDto,
      MemberDto memberDto, String auctionId) {
    return KafkaChatMessageDto.builder()
        .auctionId(auctionId)
        .memberId(messageDto.getMemberId())
        .memberNickname(memberDto.getNickname())
        .memberProfileImage(memberDto.getProfileImage())
        .message(messageDto.getMessage())
        .build();
  }
}
