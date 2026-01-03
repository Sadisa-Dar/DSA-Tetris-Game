package My;

public class Array2D {
    private final int rows;
    private final int cols;
    private int[] arr;

    public Array2D(int rows, int cols) {
        if(rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive integers");
        }
        this.rows = rows;
        this.cols = cols;
        this.arr = new int[rows * cols];
    }
    public int get(int row, int col) {
        if(!isValid(row, col)){
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return arr[index(row, col)];
    }

    public boolean isValid(int row, int col){
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public void set(int row, int col, int value) {
        if(!isValid(row, col)){
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        arr[index(row, col)] = value;
    }

    private int index(int row, int col) {
        return cols * row + col;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int length(){
        return rows;
    }

    public int totalElements(){
        return arr.length;
    }
}
