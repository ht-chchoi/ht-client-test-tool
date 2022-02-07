package com.ht.testtool.ui;

import com.ht.testtool.AppMain;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class MainFrame extends JFrame {
  private static MainFrame INSTANCE;

  public static MainFrame getInstance() {
    if (INSTANCE == null) {
      INSTANCE = MainFrame.createInstance();
    }

    return INSTANCE;
  }

  private MainFrame() throws HeadlessException {
    super();
  }

  private static MainFrame createInstance() {
    MainFrame mainFrame = new MainFrame();
    mainFrame.setTitle("HT 통합 테스트툴 [" + AppMain.APP_VERSION + "]");
    mainFrame.setContentPane(new ControllerPage().getMainPanel());
    mainFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        int confirmed = JOptionPane.showConfirmDialog(
          null,
          "모든 툴을 종료하시겠습니까?",
          "종료 확인",
          JOptionPane.YES_NO_OPTION);
        if (confirmed == JOptionPane.YES_OPTION) {
          mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
          mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
      }
    });

    mainFrame.setMinimumSize(new Dimension(400, 0));
    mainFrame.pack();

    return mainFrame;
  }
}
