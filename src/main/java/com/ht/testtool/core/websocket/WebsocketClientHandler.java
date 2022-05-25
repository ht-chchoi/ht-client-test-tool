package com.ht.testtool.core.websocket;

import com.ht.testtool.core.MainContext;
import com.ht.testtool.core.websocket.strategy.WebsocketClientHandleStrategy;
import com.ht.testtool.data.type.AccessType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebsocketClientHandler extends SimpleChannelInboundHandler<Object> {
  private final WebSocketClientHandshaker handshaker;
  private ChannelPromise handshakeFuture;
  private WebsocketClientHandleStrategy websocketClientHandleStrategy;
  private final String websocketClientId;

  public WebsocketClientHandler(WebSocketClientHandshaker handshaker,
                                WebsocketClientHandleStrategy websocketClientHandleStrategy,
                                String websocketClientId) {
    this.handshaker = handshaker;
    this.websocketClientHandleStrategy = websocketClientHandleStrategy;
    this.websocketClientId = websocketClientId;
  }

  public ChannelFuture handshakeFuture() {
    return handshakeFuture;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    this.handshakeFuture = ctx.newPromise();
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    this.handshaker.handshake(ctx.channel());
    // handshake 검증 안함
//    if (!this.handshaker.isHandshakeComplete()) {
//      ctx.disconnect();
//    }
    ctx.channel().writeAndFlush("test");
    this.accessWebsocketClientHandleStrategy(AccessType.READ, null)
        .handleChannelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    log.info("WebSocket Client disconnected!");
    this.accessWebsocketClientHandleStrategy(AccessType.READ, null).handleChannelInactive(ctx);
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) {
    Channel ch = ctx.channel();

    // handshake
    if (!handshaker.isHandshakeComplete()) {
      if (msg instanceof FullHttpResponse) {
        FullHttpResponse response = (FullHttpResponse) msg;
        if (!HttpResponseStatus.SWITCHING_PROTOCOLS.equals(response.status())) {
          log.error("비정상 connection 종료");
          return;
        }
      }

      try {
        // handshake 검증 안함
        this.handshaker.finishHandshake(ch, (FullHttpResponse) msg);
      } catch (Exception ignored) {
      }
      this.handshakeFuture.setSuccess();

      log.info("WebSocket Client connected!");
      MainContext.getInstance().getClientManager()
          .appendLogWebsocketClient(this.websocketClientId, "Websocket 연결 성공!");
      return;
    }

    if (msg instanceof FullHttpResponse) {
      FullHttpResponse response = (FullHttpResponse) msg;
      throw new IllegalStateException(
          "Unexpected FullHttpResponse (getStatus=" + response.status() +
              ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
    }

    // process message
    WebSocketFrame frame = (WebSocketFrame) msg;
    if (frame instanceof TextWebSocketFrame) {
      this.accessWebsocketClientHandleStrategy(AccessType.READ, null).handleMessage(ctx, (TextWebSocketFrame) frame);
    } else if (frame instanceof PongWebSocketFrame) {
      log.info("WebSocket Client received pong");
    } else if (frame instanceof CloseWebSocketFrame) {
      log.info("WebSocket Client received closing");
      ch.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (!this.handshakeFuture.isDone()) {
      this.handshakeFuture.setFailure(cause);
    }
    this.accessWebsocketClientHandleStrategy(AccessType.READ, null).handleExceptionCaught(ctx, cause);
    ctx.close();
  }

  public synchronized WebsocketClientHandleStrategy accessWebsocketClientHandleStrategy(
      final AccessType accessType, final WebsocketClientHandleStrategy websocketClientHandleStrategy) {
    switch (accessType) {
      case READ: {
        return this.websocketClientHandleStrategy;
      }
      case WRITE: {
        this.websocketClientHandleStrategy = websocketClientHandleStrategy;
        return this.websocketClientHandleStrategy;
      }
      default: {
        log.error("invalid AccessType, accessType: {}", accessType);
        throw new IllegalArgumentException("invalid AccessType");
      }
    }
  }
}

