public class Tetromino {

    private int[][] shape;
    private int x;          // column position on board
    private int y;          // row position on board

    public Tetromino(int[][] shape) {
        this.shape = shape;
        this.x = 3;
        this.y = 0;  // spawn at top
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
}
