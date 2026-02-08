import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.KeyEvent;
import java.awt.Font;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.io.File;

public class HardGamePanel extends JPanel {

    private static final int ROWS = 20;
    private static final int COLS = 10;
    
    private boolean gameOver = false;
    private boolean[][] board;
    private Tetromino currentBlock;
    private Image backgroundImage;
    private Image topLeftImage;

    // ---------------- SCORE & LEVEL ----------------
    private int score = 0;
    private int level = 1;
    private static final int LEVEL_UP_SCORE = 500;

    // ---------------- LEADERBOARD ----------------
    private MyMap<Integer, Integer> leaderboard = new MyMap<>();
    private int gameCount = 0;

    // ---------------- TIMER ----------------
    private Timer gravityTimer;
    private int gravityDelay = 550;

    // AI Shadow Coordinates
    private int aiBestX = -1;
    private int aiBestY = -1;

    private final Color[] COLORS = {
        new Color(255, 0, 0),    // Red
        new Color(0, 255, 0),    // Green
        new Color(0, 0, 255),    // Blue
        new Color(255, 255, 0),  // Yellow
        new Color(255, 165, 0),  // Orange
        new Color(128, 0, 128),  // Purple
        new Color(0, 255, 255)   // Cyan
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

    private void playSound(String filePath, boolean loop, float volume) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(volume); // volume in decibels (e.g., -10.0f)
                }

                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
                clip.start();
            } else {
                System.out.println("Can't find sound file: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HardGamePanel() {
        board = new boolean[ROWS][COLS];

        backgroundImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\background.jpg"
        ).getImage();

        topLeftImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\Tetris_logo.png"
        ).getImage();

        setPreferredSize(new Dimension(500, 700));
        setBackground(Color.WHITE);

        // ----------------------------
        //   AUTO FALL TIMER (Gravity)
        // ----------------------------
        gravityTimer = new Timer(gravityDelay, e -> moveDown());
        gravityTimer.start();

        //Sound
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\theme.wav", true, 0.0f);

        // Spawn first Tetromino
        spawnBlock();

        // ----------------------------
        //   key input
        // ----------------------------
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });
        setFocusable(true);
    }

    // -----------------------
    // MOVE DOWN (Gravity)
    // -----------------------
    private void moveDown() {
        if (currentBlock == null || gameOver) //hafsa
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
        if (gameOver) {
            if (keyCode == KeyEvent.VK_R) {
                restartGame();
            }
            return;
        }
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

                    if (boardY >= 0 && board[boardY][boardX])
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
                    board[by + r][bx + c] = true;
                }
            }
        }
        int cleared = clearRows() + clearCols();
        if (cleared > 0){
            addScore(cleared);
        } 
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\lock.wav", false, 1.0f);
        repaint();
    }

    // ---------------- SCORE + LEVEL ----------------
    private void addScore(int cleared) {
        if (cleared <= 0) return;
    
        int points;
        if (cleared == 1){
            points = 300;
        }
        else if (cleared == 2){
            points = 500;
        }
        else if (cleared == 3){
            points = 600;
        }
        else{
            points = 800;
        }
    
        score += points;
        updateLevel();
    }

    private void updateLevel() {
        int newLevel = (score / LEVEL_UP_SCORE) + 1;
    
        // Check if the level has actually increased
        if (newLevel > level) {
            level = newLevel;
            playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\score.wav", false, 1.5f);
            
            // Update game speed
            int newDelay = Math.max(120, 550 - (level - 1) * 50);
            gravityTimer.setDelay(newDelay);
        }
    }

    public void restartGame() {
        // Reset board
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = false;
            }
        }
    
        // Reset game variables
        score = 0;
        level = 1;
        currentBlock = null;
        gameOver = false;
    
        // Reset AI shadow
        aiBestX = -1;
        aiBestY = -1;
    
        // Restart gravity timer
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
        boolean[][] tempBoard = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++){
            tempBoard[r] = board[r].clone();
        } 
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] == 1){
                    tempBoard[y + r][x + c] = true;
                }
            }
        }
        return (-4.5 * (ROWS - y)) + (-3.4 * countRowTransitions(tempBoard)) + 
               (-3.2 * countColTransitions(tempBoard)) + (-7.9 * countHoles(tempBoard));
    }

    private int countHoles(boolean[][] b) {
        int holes = 0;
        for (int c = 0; c < COLS; c++) {
            boolean blockFound = false;
            for (int r = 0; r < ROWS; r++) {
                if (b[r][c]){
                    blockFound = true;
                }  
                else if (blockFound){
                    holes++;
                }
            }
        }
        return holes;
    }

    private int countRowTransitions(boolean[][] b) {
        int trans = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS - 1; c++) {
                if (b[r][c] != b[r][c + 1]){
                    trans++;
                } 
            }
        }
        return trans;
    }

    private int countColTransitions(boolean[][] b) {
        int trans = 0;
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS - 1; r++) {
                if (b[r][c] != b[r + 1][c]){
                    trans++;
                } 
            }
        }
        return trans;
    }

    private int getTopOccupiedRow() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c]){
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

        // -------------------- Calculate cell size --------------------
        int paddingTop = 50;
        int paddingBottom = 50;
        int availableHeight = panelHeight - paddingTop - paddingBottom;
        int cellWidth = panelWidth / COLS;
        int cellHeight = availableHeight / ROWS;
        int cellSize = Math.min(cellWidth, cellHeight);

        int xOffset = (panelWidth - (COLS * cellSize)) / 2;
        int yOffset = paddingTop;

        // -------------------- Draw Score & Level --------------------
        Font retroFont = new Font("Arial", Font.BOLD, 32);
        g2d.setFont(retroFont);
        g2d.setColor(new Color(0, 71, 118));

        String scoreText = "SCORE: " + score;
        String levelText = "LEVEL " + level;

        // Level centered above board
        int xLevel = xOffset + (COLS * cellSize) / 2 - g2d.getFontMetrics().stringWidth(levelText) / 2;
        int yLevel = yOffset - 20;

        // Score to the left of board
        int xScore = xOffset - 280;
        int yScore = yOffset + (ROWS * cellSize) / 2;

        // Draw Level and Score with subtle shadow
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                g2d.drawString(levelText, xLevel + dx, yLevel + dy);
                g2d.drawString(scoreText, xScore + dx, yScore + dy);
            }
        }

        // -------------------- Draw board cells --------------------
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(board[row][col] ? Color.BLUE : new Color(104, 187, 237, 150));
                g2d.fillRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);

                g2d.setColor(new Color(33, 137, 217));
                g2d.drawRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);
            }
        }

        // -------------------- Draw current Tetromino --------------------
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

            // Draw active block
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

        // -------------------- Draw rounded border around board --------------------
        g2d.setColor(new Color(0, 102, 168));
        g2d.setStroke(new java.awt.BasicStroke(10));
        int arc = (int) (cellSize * 0.5);
        g2d.drawRoundRect(xOffset, yOffset, COLS * cellSize, ROWS * cellSize, arc, arc);

        // -------------------- Draw Leaderboard --------------------
        int lbWidth = 300;
        int lbHeight = 400;
        int lbX = xOffset + COLS * cellSize + 100;
        int lbY = yOffset + 100;

        // -------------------- Draw leaderboard background --------------------
        g2d.setColor(new Color(104, 187, 237, 150)); // semi-transparent
        g2d.fillRoundRect(lbX, lbY, lbWidth, lbHeight, 20, 20);

        // -------------------- Draw leaderboard border --------------------
        g2d.setColor(new Color(0, 102, 168));
        g2d.setStroke(new java.awt.BasicStroke(10));
        g2d.drawRoundRect(lbX, lbY, lbWidth, lbHeight, 20, 20);

        // -------------------- Draw title --------------------
        Font retroFont1 = new Font("Arial", Font.BOLD, 28);
        g2d.setFont(retroFont1);
        g2d.setColor(new Color(0, 71, 118));
        String title = "LEADERBOARD";
        int titleX = lbX + (lbWidth - g2d.getFontMetrics().stringWidth(title)) / 2;
        int titleY = lbY + 40;
        g2d.drawString(title, titleX, titleY);

        // -------------------- Draw scores --------------------
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        int lineHeight = 30;
        int i = 1;
        int scoreYStart = titleY + 30;

        Node<Integer, Integer> current = leaderboard.getHead();
        while (current != null && i <= 10) {
            String text = i + " -> " + current.value;
            g2d.drawString(text, lbX + 20, scoreYStart + lineHeight * (i - 1));
            current = current.next;
            i++;
        }

        while (i <= 10) {
            g2d.drawString(i + " -> ----", lbX + 20, scoreYStart + lineHeight * (i - 1));
            i++;
        }

        if (gameOver) {
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.setColor(Color.BLACK);
            String msg = "GAME OVER! Press R to Restart";
            int msgX = (getWidth() - g2d.getFontMetrics().stringWidth(msg)) / 2;
            int msgY = getHeight() / 2;
            g2d.drawString(msg, msgX, msgY);
        }
        
    }


    // -----------------------
    // SPAWN NEW BLOCK
    // -----------------------
    public void spawnBlock() {
        
        int randomIndex = (int)(Math.random() * SHAPES.length);
        currentBlock = new Tetromino(SHAPES[randomIndex]);
    
        int colorIndex = (int)(Math.random() * COLORS.length);
        currentBlock.setColor(COLORS[colorIndex]);

        int shapeWidth = currentBlock.getShape()[0].length;
        int randomX = (int) (Math.random() * (COLS - shapeWidth + 1));

        currentBlock.setPosition(randomX, 0);
    
        // Reset AI shadow
        aiBestX = -1;
        aiBestY = -1;
    
        // Game Over Check
        if (!canMoveWithShape(currentBlock.getShape(), currentBlock.getX(), currentBlock.getY())) {
            gameOver = true;
            if (gravityTimer != null) gravityTimer.stop();
    
            // Only add to leaderboard if score > 0
            if (score > 0) {
                addScoreToLeaderboard(score);
            }        
    
            repaint();
            return;
        }
    
        // Compute AI shadow
        computeBestAIMove();
    
        // Adjust speed based on level
        if (gravityTimer != null) {
            int newDelay = Math.max(100, 550 - (level - 1) * 50);
            gravityTimer.setDelay(newDelay);
        }
    
        repaint();
    }            

    public Tetromino getCurrentBlock() {
        return currentBlock;
    }

    private void addScoreToLeaderboard(int newScore) {
        gameCount++;
        leaderboard.putSortedDescending(gameCount, newScore);
    
        // Keep only top 10
        while (leaderboard.size() > 10) {
            leaderboard.deleteLast();
        }
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
                if (!board[r][c]) {
                    full = false;
                    break;
                }
            }
    
            if (full) {
                cleared++;
                playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\clear.wav", false, 1.5f);
                // Shift all rows above down manually
                for (int i = r; i > 0; i--) {
                    for (int j = 0; j < COLS; j++) {
                        board[i][j] = board[i - 1][j]; 
                    }
                }
    
                // Clear the top row
                for (int j = 0; j < COLS; j++) {
                    board[0][j] = false;
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
                if (!board[r][c]) {
                    full = false;
                    break;
                }
            }
    
            if (full) {
                cleared++;
                playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\clear.wav", false, 1.5f);
                // Clear column
                for (int r = 0; r < ROWS; r++) {
                    board[r][c] = false;
                }
            }
        }
    
        return cleared;
    }    
}
