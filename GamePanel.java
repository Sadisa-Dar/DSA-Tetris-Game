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
    public boolean isColorChanged = false;

    private boolean[][] board;
    private Image backgroundImage;
    private Image topLeftImage; // small image in top-left

    public GamePanel() {
        board = new boolean[ROWS][COLS];

        // Background image
        if (isColorChanged) {
            backgroundImage = new ImageIcon(
                "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images/background.jpg"
            ).getImage();

            // Small image for top-left
        topLeftImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images/Tetris_logo.png" // replace with your image path
        ).getImage();

        }else {
            backgroundImage = new ImageIcon(
                "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images/background2.jpg"
            ).getImage();

            // Small image for top-left
        topLeftImage = new ImageIcon(
            "D:\\BS-CS\\BS-CS-3rd-Semester\\DSA by Dr. Syed Qamar Askari Shah\\Project\\Tetris\\src\\images/Tetris_logo.png" // replace with your image path
        ).getImage();
        }
    
        setPreferredSize(new Dimension(500, 700));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Draw full background
        g2d.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);

        // Draw small image at top-left (not stretched)
        int imgWidth = 250; // width of top-left image
        int imgHeight = 200; // height of top-left image
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

        // Draw cells
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g2d.setColor(board[row][col] ? Color.BLUE : new Color(104, 187, 237, 150));
                g2d.fillRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);

                g2d.setColor(new Color(33, 137, 217));
                g2d.drawRect(xOffset + col * cellSize, yOffset + row * cellSize, cellSize, cellSize);
            }
        }

        // Rounded border
        g2d.setColor(new Color(0, 102, 168));
        g2d.setStroke(new java.awt.BasicStroke(10));
        int arc = (int) (cellSize * 0.5);
        g2d.drawRoundRect(xOffset, yOffset, COLS * cellSize, ROWS * cellSize, arc, arc);
    }

    public void setCell(int row, int col, boolean filled) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            board[row][col] = filled;
            repaint();
        }
    }
}
