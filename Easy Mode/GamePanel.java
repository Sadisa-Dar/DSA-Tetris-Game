import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {

    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static int numberOfTotalCleared = 0;
    
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

    
    public GamePanel() {
        board = new boolean[ROWS][COLS];

        backgroundImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images\\background.jpg"
        ).getImage();

        topLeftImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images\\Tetris_logo.png"
        ).getImage();

        setPreferredSize(new Dimension(500, 700));
        setBackground(Color.WHITE);

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
        int[][] shape = currentBlock.getShape();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {

                if (shape[r][c] == 1) {
                    int boardX = newX + c;
                    int boardY = newY + r;

                    // Bottom boundary
                    if (boardY >= ROWS) 
                        return false;

                    // Side boundaries
                    if (boardX < 0 || boardX >= COLS) 
                        return false;

                    // Existing block collision
                    if (board[boardY][boardX]) 
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
    
        int[][] rotatedShape = currentBlock.getRotatedShape();
    
        if (canRotate(rotatedShape)) {
            currentBlock.setShape(rotatedShape);
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
                    board[by + r][bx + c] = true;
                }
            }
        }
        clearRows();
        repaint();
        clearCols();
        repaint();
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
        g2d.setStroke(new java.awt.BasicStroke(10));
        int arc = (int) (cellSize * 0.5);
        g2d.drawRoundRect(xOffset, yOffset, COLS * cellSize, ROWS * cellSize, arc, arc);
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
        repaint();
    }
    

    public Tetromino getCurrentBlock() {
        return currentBlock;
    }

    // -----------------------
    // Clearing Rows
    // -----------------------
    public void clearRows(){
        for(int row = 0; row < ROWS; row++){
            boolean full = true;
            for(int col = 0; col < COLS; col++){
                if(board[row][col] == false){
                    full = false;
                    break;
                }
            }
            if(full){
                // Clear the line
                for(int j = 0; j < COLS; j++){
                    board[row][j] = false;
                }
                // Move all lines above down
                for(int k = row; k > 0; k--){
                    for(int j = 0; j < COLS; j++){
                        board[k][j] = board[k-1][j];
                    }
                }
                // Clear the top line
                for(int j = 0; j < COLS; j++){
                    board[0][j] = false;
                }
                numberOfTotalCleared++;
                // Check the same line again
                clearRows();
                return;
            }
        }
    }
    // -----------------------
    // Clearing Columns
    // -----------------------
    public void clearCols() {
        for (int col = 0; col < COLS; col++) {
            boolean full = true;
            for (int row = 0; row < ROWS; row++) {
                if (!board[row][col]) {
                    full = false;
                    break;
                }
            }
            if (full) {
                // Clear the column
                for (int row = 0; row < ROWS; row++) {
                    board[row][col] = false;
                }
                numberOfTotalCleared++;
                // Recursive check again
                clearCols();
                return;
            }
        }
    }
}
