import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.KeyEvent;
import java.awt.Font;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D; 
import java.io.File;



public class HardGamePanel extends JPanel {

    private static final int ROWS = 20;
    private static final int COLS = 10;

    private boolean gameOver = false;
    private Array2D<Boolean> board;
    private Tetromino currentBlock;
    private Image backgroundImage;
    private Image topLeftImage;
    private int gameId = -1;

    private int score = 0;
    private int level = 1;
    private static final int LEVEL_UP_SCORE = 500;

    private LeaderboardManager manager;

    private Timer gravityTimer;
    private int gravityDelay = 550;

    private int aiBestX = -1;
    private int aiBestY = -1;

    // --- BUTTON COORDINATES (For Game Over Dialog) ---
    private int btnRestartX, btnRestartY, btnRestartW, btnRestartH;
    private int btnMenuX, btnMenuY, btnMenuW, btnMenuH;

    private final Color[] COLORS = {
            new Color(255, 0, 0), // Red
            new Color(0, 255, 0), // Green
            new Color(0, 0, 255), // Blue
            new Color(255, 255, 0), // Yellow
            new Color(255, 165, 0), // Orange
            new Color(128, 0, 128), // Purple
            new Color(0, 255, 255) // Cyan
    };

    private final int[][][] SHAPES = {
        // I shape horizontal
        { {1, 1, 1, 1} },
    
        // I shape vertical
        { {1}, {1}, {1}, {1} },
    
        // O shape
        { {1, 1},
          {1, 1} },
    
        // T shape upright
        { {0, 1, 0},
          {1, 1, 1} },
    
        // T shape upside-down
        { {1, 1, 1},
          {0, 1, 0} },
    
        // T shape rotated left
        { {1, 0},
          {1, 1},
          {1, 0} },
    
        // T shape rotated right
        { {0, 1},
          {1, 1},
          {0, 1} },
    
        // S shape horizontal
        { {0, 1, 1},
          {1, 1, 0} },
    
        // S shape vertical
        { {0, 1},
          {1, 1},
          {1, 0} },
    
        // Z shape horizontal
        { {1, 1, 0},
          {0, 1, 1} },
    
        // Z shape vertical
        { {1, 0},
          {1, 1},
          {0, 1} },
    
        // J shape
        { {1, 0, 0},
          {1, 1, 1} },
    
        // L shape
        { {0, 0, 1},
          {1, 1, 1} },
    
        // L shape rotated left horizontally
        { {1, 1, 1},
          {1, 0, 0} },
    
        // L shape rotated right horizontally
        { {1, 1, 1},
          {0, 0, 1} },

         // L shape rotated left vertically
        { {1, 1},
          {1, 0},
          {1, 0} },

        // L shape rotated right vertically
        { {1, 1},
          {0, 1},
          {0, 1} },

        // L shape vertically
        { {1, 0},
          {1, 0},
          {1, 1} },

        // L shape flipped vertically
        { {0, 1},
          {0, 1},
          {1, 1} },

        // Square shape vertical
        { {1, 1},
          {1, 1},
          {1, 1} },

        // Square shape horizontal
        { {1, 1, 1},
          {1, 1, 1}},

        // Single block
        { {1} },

        // horizontal 2-cell
        { {1, 1} },

        // vertical 2-cell
        { {1},
          {1} },

        // big L shape rotated
        { {1, 1, 1},
          {1, 0, 0},
          {1, 0, 0} },

        // big J shape rotated
        { {1, 1, 1},
          {0, 0, 1},
          {0, 0, 1} },

        // big J shape
        { {0, 0, 1},
          {0, 0, 1},
          {1, 1, 1} },

        // big L shape
        { {1, 0, 0},
          {1, 0, 0},
          {1, 1, 1} }
    };

    private Clip menuMusicClip;

    private void playSound(String filePath, boolean loop) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                    this.menuMusicClip = clip;
                }

                clip.start();
            } else {
                System.out.println("Can't find sound file: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (menuMusicClip != null) {
            if (menuMusicClip.isRunning()) {
                menuMusicClip.stop();
            }
            menuMusicClip.close();
            menuMusicClip = null; 
        }
    }

    public HardGamePanel(LeaderboardManager manager) {

        this.manager = manager;
        String mode = "HARD";
        this.level = manager.getMaxLevel(mode);
        this.gravityDelay = Math.max(120, 550 - (level - 1) * 50);
        this.gameId = manager.createGame(mode, this.level);
        manager.updateGame(gameId, score, level);

        board = new Array2D<>(ROWS, COLS);
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                board.set(r, c, false);

        backgroundImage = new ImageIcon(
                "D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\background.jpg").getImage();

        topLeftImage = new ImageIcon(
                "D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\Tetris_logo.png").getImage();

        setPreferredSize(new Dimension(500, 700));
        setBackground(Color.WHITE);

        // ----------------------------
        // AUTO FALL TIMER (Gravity)
        // ----------------------------
        gravityTimer = new Timer(gravityDelay, e -> moveDown());
        gravityTimer.start();

        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\theme.wav", true);

        spawnBlock();

        // ----------------------------
        // key input
        // ----------------------------
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });
        setFocusable(true);

        // --- MOUSE LISTENER FOR DIALOG BUTTONS ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver) {
                    handleGameOverClick(e.getX(), e.getY());
                }
            }
        });
    }

    // -----------------------
    // MOVE DOWN (Gravity)
    // -----------------------
    private void moveDown() {
        if (currentBlock == null || gameOver)
            return;

        if (!canMove(currentBlock.getX(), currentBlock.getY() + 1)) {
            lockBlock();
            spawnBlock();
            return;
        }

        currentBlock.setPosition(currentBlock.getX(), currentBlock.getY() + 1);
        repaint();
    }

    // -----------------------
    // MOVE LEFT
    // -----------------------
    public void moveLeft() {
        if (currentBlock == null)
            return;
        if (canMove(currentBlock.getX() - 1, currentBlock.getY())) {
            currentBlock.setPosition(currentBlock.getX() - 1, currentBlock.getY());
            repaint();
        }
    }

    // -----------------------
    // MOVE RIGHT
    // -----------------------
    public void moveRight() {
        if (currentBlock == null)
            return;
        if (canMove(currentBlock.getX() + 1, currentBlock.getY())) {
            currentBlock.setPosition(currentBlock.getX() + 1, currentBlock.getY());
            repaint();
        }
    }

    // -----------------------
    // HANDLE KEY INPUT
    // -----------------------
    public void handleKeyPress(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            moveLeft();
        } 
        else if (keyCode == KeyEvent.VK_RIGHT) {
            moveRight();
        } 
        else if (keyCode == KeyEvent.VK_DOWN) {
            moveDown();
        }
    }

    // -----------------------
    // COLLISION CHECK
    // -----------------------
    private boolean canMove(int newX, int newY) {
        return canMoveWithShape(currentBlock.getShape(), newX, newY);
    }

    private boolean canMoveWithShape(int[][] shape, int newX, int newY) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    int boardX = newX + c;
                    int boardY = newY + r;

                    if (boardX < 0 || boardX >= COLS || boardY >= ROWS)
                        return false;

                    if (boardY >= 0 && board.get(boardY, boardX))
                        return false;
                }
            }
        }
        return true;
    }

    // --------------------------------------
    // LOCK TETROMINO INTO THE BOARD
    // --------------------------------------
    private void lockBlock() {
        int[][] shape = currentBlock.getShape();
        int bx = currentBlock.getX();
        int by = currentBlock.getY();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1) {
                    board.set(by + r, bx + c, true);
                }
            }
        }

        if (by <= 0) {
            triggerGameOver();
            return;
        }

        int cleared = clearRows() + clearCols();
        if (cleared > 0) {
            addScore(cleared);
        }
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\lock.wav", false);
        repaint();
    }

    private void triggerGameOver() {
        gameOver = true;
        gravityTimer.stop();
        stopMusic();
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\gameOver.wav", false);
        repaint();
    }

    private void addScore(int cleared) {
        if (cleared <= 0)
            return;

        int points;
        if (cleared == 1) {
            points = 300;
        } else if (cleared == 2) {
            points = 500;
        } else if (cleared == 3) {
            points = 600;
        } else {
            points = 800;
        }

        score += points;
        updateLevel();
        manager.updateGame(gameId, score, level);

    }

    private void updateLevel() {
        int newLevel = (score / LEVEL_UP_SCORE) + 1;

        if (newLevel > level) {
            level = newLevel;
            playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\score.wav", false);

            int newDelay = Math.max(120, 550 - (level - 1) * 50);
            gravityTimer.setDelay(newDelay);
        }
    }

    public void restartGame() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board.set(r, c, false);
            }
        }

        score = 0;
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\theme.wav", true);
        String mode = "HARD";
        level = manager.getMaxLevel(mode);
        gravityDelay = Math.max(120, 550 - (level - 1) * 50);

        gameId = manager.createGame(mode, level);
        manager.updateGame(gameId, score, level);

        currentBlock = null;
        gameOver = false;

        aiBestX = -1;
        aiBestY = -1;

        if (gravityTimer != null) {
            gravityTimer.setDelay(gravityDelay);
            gravityTimer.start();
        }

        spawnBlock();
        repaint();
    }

    // -----------------------
    // Dellacherie Implemetaion
    // -----------------------
    private void computeBestAIMove() {
        double bestScore = Double.NEGATIVE_INFINITY;
        int[][] shape = currentBlock.getShape();

        for (int col = 0; col <= COLS - shape[0].length; col++) {
            int landingY = 0;
            while (canMoveWithShape(shape, col, landingY + 1)) {
                landingY++;
            }
            double score = evaluatePosition(col, landingY, shape);
            if (score > bestScore) {
                bestScore = score;
                aiBestX = col;
                aiBestY = landingY;
            }
        }
    }

    private double evaluatePosition(int x, int y, int[][] shape) {
        Array2D<Boolean> tempBoard = new Array2D<>(ROWS, COLS);
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                tempBoard.set(r, c, board.get(r, c));
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1){
                    tempBoard.set(y + r, x + c, true);
                }
            }
        }
        return (-4.5 * (ROWS - y)) + (-3.4 * countRowTransitions(tempBoard)) + 
               (-3.2 * countColTransitions(tempBoard)) + (-7.9 * countHoles(tempBoard));
    }

    private int countHoles(Array2D<Boolean> b) {
        int holes = 0;
        for (int c = 0; c < COLS; c++) {
            boolean blockFound = false;
            for (int r = 0; r < ROWS; r++) {
                if (b.get(r, c)){
                    blockFound = true;
                }  
                else if (blockFound){
                    holes++;
                }
            }
        }
        return holes;
    }

    private int countRowTransitions(Array2D<Boolean> b) {
        int trans = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS - 1; c++) {
                if (!b.get(r, c).equals(b.get(r, c + 1))){
                    trans++;
                } 
            }
        }
        return trans;
    }

    private int countColTransitions(Array2D<Boolean> b) {
        int trans = 0;
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS - 1; r++) {
                if (!b.get(r, c).equals(b.get(r + 1, c))){
                    trans++;
                } 
            }
        }
        return trans;
    }

    private int getTopOccupiedRow() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board.get(r, c)){
                    return r;
                } 
            }
        }
        return ROWS;
    }

    // -----------------------
    // PAINT METHOD
    // -----------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        g2d.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);
        int imgWidth = 250;
        int imgHeight = 200;
        g2d.drawImage(topLeftImage, 40, 40, imgWidth, imgHeight, this);

        int paddingTop = 50;
        int paddingBottom = 50;
        int availableHeight = panelHeight - paddingTop - paddingBottom;
        int cellWidth = panelWidth / COLS;
        int cellHeight = availableHeight / ROWS;
        int cellSize = Math.min(cellWidth, cellHeight);

        int xOffset = (panelWidth - (COLS * cellSize)) / 2;
        int yOffset = paddingTop;

        Font retroFont = new Font("Arial", Font.BOLD, 32);
        g2d.setFont(retroFont);
        g2d.setColor(new Color(0, 71, 118));

        String scoreText = "SCORE: " + score;
        String levelText = "LEVEL " + level;

        int xLevel = xOffset + (COLS * cellSize) / 2 - g2d.getFontMetrics().stringWidth(levelText) / 2;
        int yLevel = yOffset - 20;

        int xScore = xOffset - 280;
        int yScore = yOffset + (ROWS * cellSize) / 2;

        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                g2d.drawString(levelText, xLevel + dx, yLevel + dy);
                g2d.drawString(scoreText, xScore + dx, yScore + dy);
            }
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(board.get(row, col) ? Color.BLUE : new Color(104, 187, 237, 150));
                g2d.fillRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);

                g2d.setColor(new Color(33, 137, 217));
                g2d.drawRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);
            }
        }

        if (currentBlock != null) {
            int[][] shape = currentBlock.getShape();
            int bx = currentBlock.getX();
            int by = currentBlock.getY();
            int topRow = getTopOccupiedRow();

            // Draw AI shadow
            if (!gameOver && by >= topRow - 5) {
                g2d.setColor(new Color(200, 200, 200, 70));
                for (int r = 0; r < shape.length; r++) {
                    for (int c = 0; c < shape[r].length; c++) {
                        if (shape[r][c] == 1) {
                            g2d.fillRect(
                                xOffset + (aiBestX + c) * cellSize,
                                yOffset + (aiBestY + r) * cellSize,
                                cellSize,
                                cellSize
                            );
                        }
                    }
                }
            }

            g2d.setColor(currentBlock.getColor());
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] == 1) {
                        int drawX = xOffset + (bx + c) * cellSize;
                        int drawY = yOffset + (by + r) * cellSize;
                        g2d.fillRect(drawX, drawY, cellSize, cellSize);

                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(drawX, drawY, cellSize, cellSize);
                        g2d.setColor(currentBlock.getColor());
                    }
                }
            }
        }

        g2d.setColor(new Color(0, 102, 168));
        g2d.setStroke(new java.awt.BasicStroke(10));
        int arc = (int) (cellSize * 0.5);
        g2d.drawRoundRect(xOffset, yOffset, COLS * cellSize, ROWS * cellSize, arc, arc);

        if (gameOver) {
            stopMusic();
            
            drawGameOverScreen(g2d, panelWidth, panelHeight);
        }

    }

    // ==========================================================
    //  GAME OVER UI WITH CUSTOM DRAWN ICONS (FROM NEW CODE)
    // ==========================================================
    
    private void drawGameOverScreen(Graphics2D g2, int w, int h) {
        // 1. Full Screen Blur/Dim
        g2.setColor(new Color(10, 20, 40, 230)); 
        g2.fillRect(0, 0, w, h);
        
        // 2. Main Card Dimensions
        int dialogW = 320;
        int dialogH = 380; 
        int dialogX = (w - dialogW) / 2;
        int dialogY = (h - dialogH) / 2;
        
        // --- Main Card Background ---
        g2.setColor(new Color(20, 35, 65)); 
        g2.fillRoundRect(dialogX, dialogY, dialogW, dialogH, 40, 40);
        
        // Inner Glow
        g2.setColor(new Color(60, 100, 160)); 
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(dialogX, dialogY, dialogW, dialogH, 40, 40);
        
        // 3. "GAME OVER" Text
        g2.setFont(new Font("Verdana", Font.BOLD, 36));
        g2.setColor(new Color(255, 100, 0, 50)); // Shadow
        centerText(g2, "GAME OVER", w, dialogY + 50);
        g2.setColor(new Color(255, 140, 50));
        centerText(g2, "GAME OVER", w, dialogY + 48);
        g2.fillRect(w/2 - 40, dialogY + 60, 80, 4); // Underline

        // 4. Score Box
        int scoreBoxW = 260;
        int scoreBoxH = 120;
        int scoreBoxX = (w - scoreBoxW) / 2;
        int scoreBoxY = dialogY + 80;
        
        g2.setColor(new Color(10, 20, 40)); 
        g2.fillRoundRect(scoreBoxX, scoreBoxY, scoreBoxW, scoreBoxH, 20, 20);
        
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(100, 200, 255));
        centerText(g2, "YOUR SCORE", w, scoreBoxY + 30);
        
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(Color.WHITE);
        centerText(g2, String.format("%,d", score), w, scoreBoxY + 80);
        
        // Badge
        int badgeW = 140;
        int badgeH = 25;
        int badgeX = (w - badgeW) / 2;
        int badgeY = scoreBoxY + 105;
        g2.setColor(new Color(255, 200, 0));
        g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 25, 25);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        drawCenteredString(g2, "Nice Try !", badgeX, badgeY, badgeW, badgeH);

        // ======================================================
        // 5. BUTTONS (With Custom Icons)
        // ======================================================
        
        // --- RETRY BUTTON ---
        btnRestartW = 260;
        btnRestartH = 55;
        btnRestartX = (w - btnRestartW) / 2;
        btnRestartY = dialogY + 220;
        
        // Background
        g2.setColor(new Color(0, 150, 150)); // Shadow
        g2.fillRoundRect(btnRestartX, btnRestartY+4, btnRestartW, btnRestartH, 30, 30);
        g2.setColor(new Color(0, 230, 230)); // Face
        g2.fillRoundRect(btnRestartX, btnRestartY, btnRestartW, btnRestartH, 30, 30);
        
        // Text & Icon
        g2.setColor(new Color(0, 40, 40));
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        
        String retryText = "Retry";
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(retryText);
        int iconSize = 20;
        int gap = 10;
        int contentW = textW + gap + iconSize;
        int startX = btnRestartX + (btnRestartW - contentW) / 2;
        int textY = btnRestartY + ((btnRestartH - fm.getHeight()) / 2) + fm.getAscent();

        // Draw Text
        g2.drawString(retryText, startX, textY);
        
        // Draw Custom Retry Icon
        int iconX = startX + textW + gap;
        int iconY = btnRestartY + (btnRestartH - iconSize) / 2;
        drawCustomRetryIcon(g2, iconX, iconY, iconSize);


        // --- HOME BUTTON ---
        btnMenuW = 260;
        btnMenuH = 55;
        btnMenuX = (w - btnMenuW) / 2;
        btnMenuY = dialogY + 290;
        
        // Background
        g2.setColor(new Color(30, 30, 40)); // Shadow
        g2.fillRoundRect(btnMenuX, btnMenuY+4, btnMenuW, btnMenuH, 30, 30);
        g2.setColor(new Color(60, 70, 80)); // Face
        g2.fillRoundRect(btnMenuX, btnMenuY, btnMenuW, btnMenuH, 30, 30);
        
        // Text & Icon
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        
        String homeText = "Home";
        fm = g2.getFontMetrics();
        textW = fm.stringWidth(homeText);
        contentW = iconSize + gap + textW;
        startX = btnMenuX + (btnMenuW - contentW) / 2;
        textY = btnMenuY + ((btnMenuH - fm.getHeight()) / 2) + fm.getAscent();
        
        // Draw Custom Home Icon
        iconX = startX;
        iconY = btnMenuY + (btnMenuH - iconSize) / 2 - 2;
        drawCustomHomeIcon(g2, iconX, iconY, iconSize);
        
        // Draw Text
        g2.drawString(homeText, startX + iconSize + gap, textY);
    }
    
    // --- Helper Methods ---
    
    private void centerText(Graphics2D g, String text, int panelWidth, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (panelWidth - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
    
    private void drawCenteredString(Graphics2D g, String text, int rectX, int rectY, int rectW, int rectH) {
        FontMetrics fm = g.getFontMetrics();
        int x = rectX + (rectW - fm.stringWidth(text)) / 2;
        int y = rectY + ((rectH - fm.getHeight()) / 2) + fm.getAscent() - 3;
        g.drawString(text, x, y);
    }

    private void drawCustomRetryIcon(Graphics2D g2, int x, int y, int size) {
        g2.setStroke(new BasicStroke(3));
        g2.drawArc(x, y, size, size, 45, 270); 
        // Arrow head
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(x + size/2 + 4, y);
        arrow.lineTo(x + size/2 + 10, y + 6);
        arrow.lineTo(x + size/2 + 10, y - 6);
        arrow.closePath();
        g2.fill(arrow);
    }

    private void drawCustomHomeIcon(Graphics2D g2, int x, int y, int size) {
        Path2D roof = new Path2D.Double();
        roof.moveTo(x + size/2, y);
        roof.lineTo(x, y + size/2);
        roof.lineTo(x + size, y + size/2);
        roof.closePath();
        g2.fill(roof);
        
        int bodyW = size * 2/3;
        int bodyH = size / 2;
        int bodyX = x + (size - bodyW)/2;
        int bodyY = y + size/2;
        g2.fillRect(bodyX, bodyY, bodyW, bodyH);
    }

    // ------------------------------------------
    // HANDLE CLICKS ON DIALOG BOX
    // ------------------------------------------
    private void handleGameOverClick(int mouseX, int mouseY) {
        // Restart Logic
        if (mouseX >= btnRestartX && mouseX <= btnRestartX + btnRestartW &&
            mouseY >= btnRestartY && mouseY <= btnRestartY + btnRestartH) {
            restartGame();
        }

        // --- HOME / MENU BUTTON LOGIC ---
        if (mouseX >= btnMenuX && mouseX <= btnMenuX + btnMenuW &&
            mouseY >= btnMenuY && mouseY <= btnMenuY + btnMenuH) {
            
            // 1. Timer Stop
            if (gravityTimer != null) gravityTimer.stop();
            
            // 2. Switch to GameMenu
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.getContentPane().removeAll();
                
                GameMenu menu = new GameMenu(); 
                topFrame.add(menu);
                
                topFrame.revalidate();
                topFrame.repaint();
                menu.requestFocusInWindow();
            }
        }
    }


    // -----------------------
    // SPAWN NEW BLOCK
    // -----------------------
    public void spawnBlock() {

        int randomIndex = (int) (Math.random() * SHAPES.length);
        currentBlock = new Tetromino(SHAPES[randomIndex]);

        int colorIndex = (int) (Math.random() * COLORS.length);
        currentBlock.setColor(COLORS[colorIndex]);

        int shapeWidth = currentBlock.getShape()[0].length;
        int randomX = (int) (Math.random() * (COLS - shapeWidth + 1));

        currentBlock.setPosition(randomX, 0);

        aiBestX = -1;
        aiBestY = -1;

        if (!canMoveWithShape(currentBlock.getShape(), currentBlock.getX(), currentBlock.getY())) {
            gameOver = true;
            if (gravityTimer != null)
                gravityTimer.stop();

            manager.updateGame(gameId, score, level);

            repaint();
            return;
        }

        computeBestAIMove();

        if (gravityTimer != null) {
            int newDelay = Math.max(100, 550 - (level - 1) * 50);
            gravityTimer.setDelay(newDelay);
        }

        repaint();
    }

    public Tetromino getCurrentBlock() {
        return currentBlock;
    }

    /***********Sadisa***********/
    // -----------------------
    // Clearing Rows
    // -----------------------
    public int clearRows() {
        int cleared = 0;
    
        for (int r = 0; r < ROWS; r++) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (!board.get(r, c)) {
                    full = false;
                    break;
                }
            }
    
            if (full) {
                cleared++;
                playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\clear.wav", false);
                for (int i = r; i > 0; i--) {
                    for (int j = 0; j < COLS; j++) {
                        board.set(i, j, board.get(i - 1, j));
                    }
                }
    
                for (int j = 0; j < COLS; j++) {
                    board.set(0, j, false);
                }
    
                r--;
            }
        }
    
        return cleared;
    }
      

    // -----------------------
    // Clearing Columns
    // -----------------------
    public int clearCols() {
        int cleared = 0;
    
        for (int c = 0; c < COLS; c++) {
            boolean full = true;
            for (int r = 0; r < ROWS; r++) {
                if (!board.get(r, c)) {
                    full = false;
                    break;
                }
            }
    
            if (full) {
                cleared++;
                playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\clear.wav", false);
                for (int r = 0; r < ROWS; r++) {
                    board.set(r, c, false);
                }
            }
        }
    
        return cleared;
    }    
}