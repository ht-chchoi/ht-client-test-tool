package com.ht.testtool.core.websocket;

import com.ht.testtool.core.MainContext;
import com.ht.testtool.core.websocket.strategy.SimpleWebsocketClientHandleStrategy;
import com.ht.testtool.data.dto.ConnectionInfo;
import com.ht.testtool.data.type.AccessType;
import com.ht.testtool.data.type.WebsocketClientType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebsocketClient {
  private final JFrame parentFrame;
  private final String websocketClientId;
  private Channel channel;

  public WebsocketClient(final JFrame parentFrame, final String websocketClientId) {
    this.parentFrame = parentFrame;
    this.websocketClientId = websocketClientId;
  }

  public void connect(final String websocketClientId, final String autoResponseBody,
                      final ConnectionInfo connectionInfo, final WebsocketClientType websocketClientType) throws InterruptedException, URISyntaxException {
    URI connectionUri = connectionInfo.createUri();

    WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory
        .newHandshaker(connectionUri, WebSocketVersion.V13,
            null, true, new DefaultHttpHeaders());
    webSocketClientHandshaker.setForceCloseTimeoutMillis(2000);

    // TODO: websocketClientType에 따른 분기, ex)
    WebsocketClientHandler websocketClientHandler = new WebsocketClientHandler(
        webSocketClientHandshaker,
        new SimpleWebsocketClientHandleStrategy(autoResponseBody, connectionInfo, websocketClientId),
        websocketClientId);

    Bootstrap bootstrap = new Bootstrap();
    bootstrap
        .group(MainContext.getInstance().getClientManager().getEventLoopGroup())
        .channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(final SocketChannel ch) throws Exception {
            ChannelPipeline channelPipeline = ch.pipeline();
            channelPipeline
                .addLast(MainContext.getInstance().getClientManager().getSslContext()
                    .newHandler(ch.alloc(), connectionInfo.getIp(), connectionInfo.getPort()))
                .addLast(new HttpClientCodec(),
                    new HttpObjectAggregator(8192),
                    WebSocketClientCompressionHandler.INSTANCE,
                    websocketClientHandler);
          }
        });
    ChannelFuture channelFuture = bootstrap.connect(connectionUri.getHost(), connectionInfo.getPort())
        .addListener((FutureListener<Void>) future -> {
          if (!future.isSuccess()) {
            websocketClientHandler
                .accessWebsocketClientHandleStrategy(AccessType.READ, null)
                .handleExceptionCaught(future.cause());
          }
        })
        .sync();

    this.channel = channelFuture.channel();
    LocalDateTime start = LocalDateTime.now();
    websocketClientHandler
        .handshakeFuture()
        .await(2, TimeUnit.SECONDS);
    if (LocalDateTime.now().isAfter(start.plus(2L, ChronoUnit.SECONDS))) {
      websocketClientHandler
          .accessWebsocketClientHandleStrategy(AccessType.READ, null)
          .handleExceptionCaught(new RuntimeException("Connection Time out"));
      this.channel.close();
    }
  }

  public void disconnect() {
    log.info("disconnect websocketClient, websocketClientId: {}", this.websocketClientId);
    if (this.channel == null) {
      return;
    }
    if (this.channel.isActive() || this.channel.isOpen() || this.channel.isRegistered() || this.channel.isWritable()) {
      this.channel.disconnect();
    }
  }

  public String getWebsocketClientId() {
    return websocketClientId;
  }

  public JFrame getParentFrame() {
    return parentFrame;
  }
}
