package com.jeontongju.auction.dto.socket;

import com.jeontongju.auction.dto.request.ChatMessageRequestDto;
import com.jeontongju.auction.util.Mosaic;
import io.github.bitbox.bitbox.dto.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

  private String auctionId;
  private Long memberId;
  private String memberNickname;
  private String memberProfileImage;
  private String message;

  public static ChatMessageDto toKafkaChatMessageDto(
      ChatMessageRequestDto messageDto,
      MemberDto memberDto, String auctionId) {
    return ChatMessageDto.builder()
        .auctionId(auctionId)
        .memberId(messageDto.getMemberId())
        .memberNickname(Mosaic.nameMosaic(memberDto.getNickname()))
        .memberProfileImage(memberDto.getProfileImage())
        .message(messageDto.getMessage())
        .build();
  }

  public static ChatMessageDto to(String auctionId, Long memberId, String memberNickname,
      String memberProfileImage, String message) {
    return ChatMessageDto.builder()
        .auctionId(auctionId)
        .memberId(memberId)
        .memberNickname(memberNickname)
        .memberProfileImage(memberProfileImage)
        .message(message)
        .build();
  }
}
