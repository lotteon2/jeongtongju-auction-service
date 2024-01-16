package com.jeontongju.auction.config;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

@Service
public class WebSocketCustomHandler extends SubProtocolWebSocketHandler {

  /**
   * Create a new {@code SubProtocolWebSocketHandler} for the given inbound and outbound channels.
   *
   * @param clientInboundChannel  the inbound {@code MessageChannel}
   * @param clientOutboundChannel the outbound {@code MessageChannel}
   */
  public WebSocketCustomHandler(MessageChannel clientInboundChannel,
      SubscribableChannel clientOutboundChannel) {
    super(clientInboundChannel, clientOutboundChannel);
  }

}
