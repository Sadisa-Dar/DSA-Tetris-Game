import javax.swing.JFrame;

public class RunFile{

    public static void main(String[] args){
        JFrame my_frame = new JFrame("Tetris Game");

        my_frame.setSize(400, 600);
        my_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        my_frame.setResizable(false);
        my_frame.setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel();
        inserGamePanel(my_frame, gamePanel);
        my_frame.setVisible(true);

    }//main

    public static void inserGamePanel(JFrame frame, GamePanel panel){
        frame.add(panel);
    }
    
}//class