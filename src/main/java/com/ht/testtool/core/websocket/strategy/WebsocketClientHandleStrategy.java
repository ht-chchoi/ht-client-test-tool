package com.ht.testtool.core.websocket.strategy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public interface WebsocketClientHandleStrategy {
  void handleMessage(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame);
  default void handleChannelActive(ChannelHandlerContext ctx) {}
  default void handleChannelInactive(ChannelHandlerContext ctx) {}
  default void handleExceptionCaught(Throwable cause) {}
  default void handleExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {}
}
