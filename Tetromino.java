import java.awt.Color;

public class Tetromino {

    private int[][] shape;
    private int x;          // column position on board
    private int y;          // row position on board
    private Color color;

    public Tetromino(int[][] shape) {
        this.shape = shape;
        this.x = 3;
        this.y = 0;  // spawn at top
        this.color = Color.RED; // default color
    }

    public int[][] getShape() {
        return shape;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Color getColor() { 
        return color; 
    }

    public void setColor(Color color) { 
        this.color = color; 
    }
    // -----------------------
    // RETURN ROTATED SHAPE (CLOCKWISE)
    // -----------------------
    public int[][] getRotatedShape(int[][] shape) {

        int rows = shape.length;
        int cols = shape[0].length;

        int[][] rotated = new int[cols][rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = shape[r][c];
            }
        }
        return rotated;
    }
    // -----------------------
    // UPDATE SHAPE AFTER ROTATION
    // -----------------------
    public void setShape(int[][] newShape) {
        this.shape = newShape;
    }


}
