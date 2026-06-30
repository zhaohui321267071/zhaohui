package com.zhaohui.game;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class GamePanel extends JPanel implements ActionListener {
    private static final int WIDTH = 420;
    private static final int HEIGHT = 720;
    private static final int PLAYER_SIZE = 34;
    private static final int TIMER_DELAY_MS = 16;
    private static final int COIN_SCORE = 50;

    private final Timer timer = new Timer(TIMER_DELAY_MS, this);
    private final Random random = new Random();
    private final List<FallingObject> hazards = new ArrayList<>();
    private final List<FallingObject> coins = new ArrayList<>();
    private final boolean[] keys = new boolean[256];

    private int playerX;
    private int playerY;
    private int score;
    private int frames;
    private int hazardCooldown;
    private int coinCooldown;
    private boolean gameOver;

    GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(new InputHandler());
        resetGame();
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!gameOver) {
            updateGame();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(g);
        drawCoins(g);
        drawHazards(g);
        drawPlayer(g);
        drawHud(g);
        if (gameOver) {
            drawGameOver(g);
        }
        g.dispose();
    }

    private void resetGame() {
        playerX = WIDTH / 2 - PLAYER_SIZE / 2;
        playerY = HEIGHT - 100;
        score = 0;
        frames = 0;
        hazardCooldown = 0;
        coinCooldown = 45;
        gameOver = false;
        hazards.clear();
        coins.clear();
        for (int i = 0; i < keys.length; i++) {
            keys[i] = false;
        }
    }

    private void updateGame() {
        frames++;
        score += frames % 10 == 0 ? 1 : 0;
        movePlayer();
        spawnObjects();
        updateObjects(hazards, false);
        updateObjects(coins, true);
    }

    private void movePlayer() {
        int speed = 6;
        if (isPressed(KeyEvent.VK_LEFT) || isPressed(KeyEvent.VK_A)) {
            playerX -= speed;
        }
        if (isPressed(KeyEvent.VK_RIGHT) || isPressed(KeyEvent.VK_D)) {
            playerX += speed;
        }
        if (isPressed(KeyEvent.VK_UP) || isPressed(KeyEvent.VK_W)) {
            playerY -= speed;
        }
        if (isPressed(KeyEvent.VK_DOWN) || isPressed(KeyEvent.VK_S)) {
            playerY += speed;
        }
        playerX = clamp(playerX, 12, WIDTH - PLAYER_SIZE - 12);
        playerY = clamp(playerY, 90, HEIGHT - PLAYER_SIZE - 18);
    }

    private void spawnObjects() {
        int difficulty = Math.min(7, frames / 600);
        hazardCooldown--;
        coinCooldown--;
        if (hazardCooldown <= 0) {
            int size = 28 + random.nextInt(24);
            hazards.add(new FallingObject(random.nextInt(WIDTH - size - 24) + 12, -size, size, 4 + difficulty + random.nextInt(4)));
            hazardCooldown = Math.max(14, 38 - difficulty * 3);
        }
        if (coinCooldown <= 0) {
            int size = 24;
            coins.add(new FallingObject(random.nextInt(WIDTH - size - 24) + 12, -size, size, 4 + difficulty));
            coinCooldown = 70 + random.nextInt(65);
        }
    }

    private void updateObjects(List<FallingObject> objects, boolean coin) {
        Iterator<FallingObject> iterator = objects.iterator();
        while (iterator.hasNext()) {
            FallingObject object = iterator.next();
            object.y += object.speed;
            if (object.y > HEIGHT + object.size) {
                iterator.remove();
                continue;
            }
            if (intersectsPlayer(object)) {
                iterator.remove();
                if (coin) {
                    score += COIN_SCORE;
                } else {
                    gameOver = true;
                    break;
                }
            }
        }
    }

    private boolean intersectsPlayer(FallingObject object) {
        int playerCenterX = playerX + PLAYER_SIZE / 2;
        int playerCenterY = playerY + PLAYER_SIZE / 2;
        int objectCenterX = object.x + object.size / 2;
        int objectCenterY = object.y + object.size / 2;
        int hitDistance = PLAYER_SIZE / 2 + object.size / 2 - 4;
        int dx = playerCenterX - objectCenterX;
        int dy = playerCenterY - objectCenterY;
        return dx * dx + dy * dy <= hitDistance * hitDistance;
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(new Color(18, 18, 28));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(35, 35, 54));
        for (int y = 0; y < HEIGHT; y += 48) {
            g.drawLine(0, y + frames % 48, WIDTH, y + frames % 48);
        }
        g.setColor(new Color(255, 42, 109, 70));
        g.fillRoundRect(12, 78, 8, HEIGHT - 96, 12, 12);
        g.setColor(new Color(37, 244, 238, 70));
        g.fillRoundRect(WIDTH - 20, 78, 8, HEIGHT - 96, 12, 12);
    }

    private void drawPlayer(Graphics2D g) {
        g.setColor(new Color(37, 244, 238));
        g.fill(new Ellipse2D.Double(playerX - 4, playerY + 3, PLAYER_SIZE, PLAYER_SIZE));
        g.setColor(new Color(255, 42, 109));
        g.fill(new Ellipse2D.Double(playerX + 4, playerY - 3, PLAYER_SIZE, PLAYER_SIZE));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE));
        g.setColor(new Color(18, 18, 28));
        g.fillOval(playerX + 10, playerY + 10, 5, 5);
        g.fillOval(playerX + 21, playerY + 10, 5, 5);
    }

    private void drawHazards(Graphics2D g) {
        g.setStroke(new BasicStroke(3));
        for (FallingObject hazard : hazards) {
            g.setColor(new Color(255, 42, 109));
            g.fill(new RoundRectangle2D.Double(hazard.x, hazard.y, hazard.size, hazard.size, 12, 12));
            g.setColor(Color.WHITE);
            g.drawLine(hazard.x + 8, hazard.y + 8, hazard.x + hazard.size - 8, hazard.y + hazard.size - 8);
            g.drawLine(hazard.x + hazard.size - 8, hazard.y + 8, hazard.x + 8, hazard.y + hazard.size - 8);
        }
    }

    private void drawCoins(Graphics2D g) {
        for (FallingObject coin : coins) {
            g.setColor(new Color(255, 210, 54));
            g.fillOval(coin.x, coin.y, coin.size, coin.size);
            g.setColor(new Color(255, 248, 160));
            g.fillOval(coin.x + 6, coin.y + 5, 7, 7);
            g.setColor(new Color(161, 105, 0));
            g.drawOval(coin.x + 4, coin.y + 4, coin.size - 8, coin.size - 8);
        }
    }

    private void drawHud(Graphics2D g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(Color.WHITE);
        g.drawString("金币逃跑", 18, 34);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(new Color(255, 210, 54));
        g.drawString("分数: " + score, 18, 62);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        drawCentered(g, "逃跑失败", HEIGHT / 2 - 40);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        drawCentered(g, "最终分数: " + score, HEIGHT / 2 + 8);
        g.setColor(new Color(37, 244, 238));
        drawCentered(g, "按空格重新开始", HEIGHT / 2 + 52);
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (WIDTH - metrics.stringWidth(text)) / 2, y);
    }

    private boolean isPressed(int keyCode) {
        return keyCode >= 0 && keyCode < keys.length && keys[keyCode];
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private final class InputHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.VK_SPACE && gameOver) {
                resetGame();
                return;
            }
            if (keyCode >= 0 && keyCode < keys.length) {
                keys[keyCode] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (keyCode >= 0 && keyCode < keys.length) {
                keys[keyCode] = false;
            }
        }
    }

    private static final class FallingObject {
        private final int x;
        private final int size;
        private final int speed;
        private int y;

        private FallingObject(int x, int y, int size, int speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }
    }
}
