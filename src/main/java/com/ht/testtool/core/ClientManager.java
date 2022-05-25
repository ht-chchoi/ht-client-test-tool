package com.ht.testtool.core;

import com.ht.testtool.core.websocket.WebsocketClient;
import com.ht.testtool.exception.MaxConnectionException;
import com.ht.testtool.ui.MainFrame;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import javax.swing.*;

@Slf4j
public class ClientManager {
  private final EventLoopGroup eventLoopGroup;
  private final SslContext sslContext;

  private final int WEBSOCKET_MAX_CONNECTION;

  public ClientManager() {
    SslContext sslContext;
    this.eventLoopGroup = new NioEventLoopGroup();
    try {
      sslContext = SslContextBuilder
          .forClient()
          .sslProvider(SslProvider.JDK)
          .trustManager(InsecureTrustManagerFactory.INSTANCE)
          .build();
    } catch (SSLException e) {
      sslContext = null;
      log.error("[Constructor]", e);
      MainFrame.exitWithErrorDialog("SSL 로드 실패!");
    }

    this.sslContext = sslContext;
    WEBSOCKET_MAX_CONNECTION = Integer.parseInt(PropertyLoader.getInstance().getProperty("WEBSOCKET_MAX_CONNECTION"));
  }

  public String createWebsocketClient(final JFrame parentFrame, final String websocketClientId) {
    if (MainContext.getInstance().getWebsocketClientMap().size() + 1 > WEBSOCKET_MAX_CONNECTION) {
      log.error("Too Many Websocket Connection");
      JOptionPane.showMessageDialog(null, "Too Many Websocket Connection, currentConnection: "
          + MainContext.getInstance().getWebsocketClientMap().size());
      throw new MaxConnectionException();
    }

    WebsocketClient websocketClient = new WebsocketClient(parentFrame, websocketClientId);
    MainContext.getInstance().putWebsocketClient(websocketClient.getWebsocketClientId(), websocketClient);

    return websocketClient.getWebsocketClientId();
  }

  public EventLoopGroup getEventLoopGroup() {
    return eventLoopGroup;
  }

  public SslContext getSslContext() {
    return sslContext;
  }

  public void showMessageDialogWebsocketClient(final String websocketClientId, final String message) {
    WebsocketClient websocketClient = MainContext.getInstance()
        .getWebsocketClientById(websocketClientId);
    if (websocketClient == null) {
      log.warn("no websocketClient to show messageDialog, websocketClientId: {}", websocketClientId);
      return;
    }

    JOptionPane.showMessageDialog(websocketClient.getParentFrame().getContentPane(), message);
  }

  public void appendLogWebsocketClient(final String websocketClientId, final String message) {
    WebsocketClient websocketClient = MainContext.getInstance()
        .getWebsocketClientById(websocketClientId);
    if (websocketClient == null) {
      log.warn("no websocketClient to show messageDialog, websocketClientId: {}", websocketClientId);
      return;
    }

    // TODO: 메인 컨텍스트로 등록해서 사용하기
    JPanel websocketPageMainPanel = (JPanel) websocketClient.getParentFrame().getContentPane();
    JTextArea taConsole = (JTextArea) ((JPanel) websocketPageMainPanel.getComponent(3)).getComponent(1);
    taConsole.append("\n");
    taConsole.append(message);
  }
}
