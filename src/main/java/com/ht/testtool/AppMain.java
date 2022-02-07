package com.ht.testtool;

import com.ht.testtool.ui.MainFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppMain {
  public static final String APP_VERSION = "0.1";

  public static void main(String[] args) {
    log.info("===== App Start Ready =====");
    MainFrame.getInstance().setVisible(true);
    log.info("===== App Start Complete =====");
  }
}
