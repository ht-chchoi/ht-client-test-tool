package com.ht.testtool.core.websocket.strategy;

import com.ht.testtool.core.MainContext;
import com.ht.testtool.data.dto.ConnectionInfo;
import com.ht.testtool.data.type.AccessType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleWebsocketClientHandleStrategy implements WebsocketClientHandleStrategy {
  private String autoResponseBody;
  private final ConnectionInfo connectionInfo;
  private final String websocketClientId;

  public SimpleWebsocketClientHandleStrategy(String autoResponseBody, ConnectionInfo connectionInfo, String websocketClientId) {
    this.autoResponseBody = autoResponseBody;
    this.connectionInfo = connectionInfo;
    this.websocketClientId = websocketClientId;
  }

  @Override
  public void handleMessage(final ChannelHandlerContext ctx, final TextWebSocketFrame textWebSocketFrame) {
    log.info(textWebSocketFrame.text());
  }

  @Override
  public void handleChannelInactive(final ChannelHandlerContext ctx) {
    MainContext.getInstance()
        .getClientManager()
        .appendLogWebsocketClient(websocketClientId, "inactive channel");
  }

  @Override
  public void handleExceptionCaught(final Throwable cause) {
    MainContext.getInstance()
        .getClientManager()
        .showMessageDialogWebsocketClient(websocketClientId, "exception: " + cause.getMessage());
  }

  @Override
  public void handleExceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    this.handleExceptionCaught(cause);
    ctx.disconnect();
  }

  public synchronized String accessAutoResponseBody(final AccessType accessType, final String autoResponseBody) {
    switch (accessType) {
      case READ: {
        return this.autoResponseBody;
      }
      case WRITE: {
        this.autoResponseBody = autoResponseBody;
        return this.autoResponseBody;
      }
      default: {
        log.error("invalid AccessType, accessType: {}", accessType);
        throw new IllegalArgumentException("invalid AccessType");
      }
    }
  }
}
