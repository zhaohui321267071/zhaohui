package com.zhaohui.game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class DouyinRunnerGame {
    private DouyinRunnerGame() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("抖音风格躲避逃跑小游戏");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new GamePanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
