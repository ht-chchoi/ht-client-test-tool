package com.ht.testtool.ui;

import com.ht.testtool.core.MainContext;
import com.ht.testtool.core.websocket.WebsocketClient;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class WebsocketFrame extends JFrame {
  private final String websocketClientId;

  public WebsocketFrame(final String websocketClientId) throws HeadlessException {
    this.websocketClientId = websocketClientId;
    this.initView(websocketClientId);
  }

  private void initView(final String websocketClientId) {
    this.setTitle("HT 웹소켓 Tool");
    this.setContentPane(new WebsocketPage(websocketClientId).getMainPanel());
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        log.info("close websocketFrame, websocketClientId: {}", websocketClientId);
        WebsocketClient websocketClient = MainContext.getInstance().getWebsocketClientMap().remove(websocketClientId);
        websocketClient.disconnect();
      }
    });

    this.setMinimumSize(new Dimension(900, 0));
    this.pack();
  }

  public String getWebsocketClientId() {
    return websocketClientId;
  }
}
