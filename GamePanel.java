import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class GamePanel extends JPanel {

    private static final int ROWS = 20;
    private static final int COLS = 10;

    private boolean[][] board;
    private Tetromino currentBlock;
    private Image backgroundImage;
    private Image topLeftImage;

    // Tetromino shapes
    private final int[][][] SHAPES = {
        { {1, 1, 1, 1} },          // I shape
        { {1, 1}, {1, 1} },        // O shape
        { {0, 1, 0}, {1, 1, 1} },  // T shape
        { {0, 1, 1},
          {1, 1, 0} },  // S shape
        { {1, 1, 0}, {0, 1, 1} },  // Z shape
        { {1, 0, 0}, {1, 1, 1} },  // J shape
        { {0, 0, 1}, {1, 1, 1} }   // L shape
    };

    public GamePanel() {
        board = new boolean[ROWS][COLS];

        // Background image
        backgroundImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images\\background.jpg"
        ).getImage();

        // Top-left image
        topLeftImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images\\Tetris_logo.png"
        ).getImage();

        setPreferredSize(new Dimension(500, 700));
        setBackground(Color.WHITE);

        // Spawn first Tetromino
        spawnBlock();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Draw full background
        g2d.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);

        // Draw small image at top-left
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

            g2d.setColor(Color.RED);

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] == 1) {
                        int drawX = xOffset + (bx + c) * cellSize;
                        int drawY = yOffset + (by + r) * cellSize;
                        g2d.fillRect(drawX, drawY, cellSize, cellSize);

                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(drawX, drawY, cellSize, cellSize);
                        g2d.setColor(Color.RED);
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

    // Set cell on board
    public void setCell(int row, int col, boolean filled) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            board[row][col] = filled;
            repaint();
        }
    }

    // Spawn random Tetromino
    public void spawnBlock() {
        int randomIndex = (int)(Math.random() * SHAPES.length);
        currentBlock = new Tetromino(SHAPES[randomIndex]);
        currentBlock.setPosition(3, 0);  // spawn top center
        repaint();
    }

    // Getter
    public Tetromino getCurrentBlock() {
        return currentBlock;
    }
}

