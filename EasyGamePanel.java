import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.KeyEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class EasyGamePanel extends JPanel {

    private static final int ROWS = 20;
    private static final int COLS = 10;
    
    private boolean[][] board;
    private Tetromino currentBlock;
    private Image backgroundImage;
    private Image topLeftImage;
    private final Color[] COLORS = {
        new Color(255, 0, 0),    // Red
        new Color(0, 255, 0),    // Green
        new Color(0, 0, 255),    // Blue
        new Color(255, 255, 0),  // Yellow
        new Color(255, 165, 0),  // Orange
        new Color(128, 0, 128),  // Purple
        new Color(0, 255, 255)   // Cyan
    };

    private Timer gravityTimer;

    // AI Shadow Coordinates
    private int aiBestX = -1;
    private int aiBestY = -1;
    
    private final int[][][] SHAPES = {
        // I shape
        { {1, 1, 1, 1} },

        // O shape
        { {1, 1},
          {1, 1} },

        // T shape
        { {0, 1, 0},
          {1, 1, 1} },

        // S shape
        { {0, 1, 1},
          {1, 1, 0} },

        // Z shape
        { {1, 1, 0},
          {0, 1, 1} },

        // J shape
        { {1, 0, 0},
          {1, 1, 1} },

        // L shape
        { {0, 0, 1},
          {1, 1, 1} }
    };

    private void playSound(String filePath, boolean loop) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
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

    
    public EasyGamePanel() {
        board = new boolean[ROWS][COLS];

        backgroundImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\background.jpg"
        ).getImage();

        topLeftImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\Tetris_logo.png"
        ).getImage();

        setPreferredSize(new Dimension(500, 700));
        setBackground(Color.WHITE);

        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\theme.wav", true);
        spawnBlock();

        // ----------------------------
        //   AUTO FALL TIMER (Gravity)
        // ----------------------------
        gravityTimer = new Timer(550, e -> moveDown());
        gravityTimer.start();

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
        if (currentBlock == null) 
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
        else if (keyCode == KeyEvent.VK_SPACE) {
            rotateBlock();
        }
    }

    // -----------------------
    // COLLISION CHECK 
    // -----------------------
    private boolean canMove(int newX, int newY) {
        return canMoveWithShape(currentBlock.getShape(), newX, newY);
    }

    // -----------------------
    // COLLISION CHECK
    // -----------------------
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

    // -----------------------
    // ROTATE CURRENT BLOCK (FINAL)
    // -----------------------
    private void rotateBlock() {
        if (currentBlock == null)
            return;
    
        int[][] rotatedShape = currentBlock.getRotatedShape(currentBlock.getShape());
    
        if (canRotate(rotatedShape)) {
            currentBlock.setShape(rotatedShape);
            computeBestAIMove(); 
            repaint();
        }
    }
    
    // -----------------------
    // CHECK IF ROTATION IS SAFE
    // -----------------------
    private boolean canRotate(int[][] rotatedShape) {
        int bx = currentBlock.getX();
        int by = currentBlock.getY();

        for (int r = 0; r < rotatedShape.length; r++) {
            for (int c = 0; c < rotatedShape[r].length; c++) {
                if (rotatedShape[r][c] == 1) {
                    int boardX = bx + c;
                    int boardY = by + r;

                    // Boundary checks
                    if (boardX < 0 || boardX >= COLS || boardY < 0 || boardY >= ROWS)
                        return false;

                    // Collision check
                    if (board[boardY][boardX])
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
                    if (by + r >= 0 && by + r < ROWS && bx + c >= 0 && bx + c < COLS) {
                        board[by + r][bx + c] = true;
                    }
                }
            }
        }
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\lock.wav", false);
        clearRows(); 
        clearCols();
        repaint();
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

        // Reset AI variables and Compute Best Move
        aiBestX = -1;
        aiBestY = -1;
        computeBestAIMove();

        repaint();
    }
    

    public Tetromino getCurrentBlock() {
        return currentBlock;
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
                    if (y + r < ROWS && x + c < COLS) {
                        tempBoard[y + r][x + c] = true;
                    }
                }
            }
        }

        return (-4.5 * (ROWS - y)) + 
               (-3.4 * countRowTransitions(tempBoard)) + 
               (-3.2 * countColTransitions(tempBoard)) + 
               (-7.9 * countHoles(tempBoard));
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

        // Padding for top/bottom
        int paddingTop = 50;
        int paddingBottom = 50;

        // Cell size calculations
        int availableHeight = panelHeight - paddingTop - paddingBottom;
        int cellWidth = panelWidth / COLS;
        int cellHeight = availableHeight / ROWS;
        int cellSize = Math.min(cellWidth, cellHeight);

        int xOffset = (panelWidth - (COLS * cellSize)) / 2;
        int yOffset = paddingTop;

        // Draw board cells
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(board[row][col] ? Color.BLUE : new Color(104, 187, 237, 150));
                g2d.fillRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);

                g2d.setColor(new Color(33, 137, 217));
                g2d.drawRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);
            }
        }

        // Draw current Tetromino
        if (currentBlock != null) {
            int[][] shape = currentBlock.getShape();
            int bx = currentBlock.getX();
            int by = currentBlock.getY();
            
            //DRAW AI SHADOW
            if (aiBestX != -1 && aiBestY != -1) {
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
                            g2d.setColor(new Color(200, 200, 200, 70));
                        }
                    }
                }
            }
            // --------------------------------------------------------------

            // Draw Active block
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

        // Draw rounded border
        g2d.setColor(new Color(0, 102, 168));
        g2d.setStroke(new BasicStroke(10));
        int arc = (int) (cellSize * 0.5);
        g2d.drawRoundRect(xOffset, yOffset, COLS * cellSize, ROWS * cellSize, arc, arc);
    }

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
                playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\clear.wav", false);
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
                playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\clear.wav", false);
                // Clear column
                for (int r = 0; r < ROWS; r++) {
                    board[r][c] = false;
                }
            }
        }
        return cleared;
    }
}