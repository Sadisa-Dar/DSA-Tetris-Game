import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris Game");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create and add GamePanel
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);

            frame.pack(); // Use GamePanel preferred size
            frame.setLocationRelativeTo(null); // Center window

            frame.setResizable(true); // Allow resizing
            frame.setVisible(true);
        });
    }
}
