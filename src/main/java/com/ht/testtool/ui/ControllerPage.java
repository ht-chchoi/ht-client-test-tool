package com.ht.testtool.ui;

import javax.swing.*;

public class ControllerPage {
  private JPanel MainPanel;
  private JButton BtnSocketServer;
  private JButton BtnSocketClient;
  private JButton BtnWebsocket;

  public ControllerPage() {
    BtnWebsocket.addActionListener(e -> {

    });
  }

  public JPanel getMainPanel() {
    return MainPanel;
  }
}
