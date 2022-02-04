package com.ht.testtool;

import com.ht.testtool.testt.TestApp;
import javax.swing.*;

public class AppMain {
  public static void main(String[] args) {
    JFrame frame = new JFrame("App");
    frame.setContentPane(new TestApp().getMainPanel());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
