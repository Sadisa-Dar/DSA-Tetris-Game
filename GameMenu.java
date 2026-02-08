import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class GameMenu extends JPanel {
    private Image logo, background;
    private final String[] buttonLabels = {"EASY MODE", "HARD MODE"};
    private int hoveredIndex = -1; // Tracks which button is hovered (-1 means none)

    private Clip menuMusicClip; // Defined at the top of your class

    private void playSound(String filePath, boolean loop) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                    // Store this specific clip so stopMusic() can kill it later
                    this.menuMusicClip = clip; 
                }
                
                clip.start();
            } else {
                System.out.println("Can't find sound file: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (menuMusicClip != null) {
            if (menuMusicClip.isRunning()) {
                menuMusicClip.stop();
            }
            menuMusicClip.close();
            menuMusicClip = null; // Clear it out to be safe
        }
    }

    public GameMenu() {
        logo = new ImageIcon("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\Tetris_logo.png").getImage();
        background = new ImageIcon("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\b6.jpg").getImage();
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\damtaro.wav", true);

        // --- HOVER LOGIC ---
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int previousHover = hoveredIndex;
                hoveredIndex = -1; // Reset to none

                for (int i = 0; i < buttonLabels.length; i++) {
                    if (getButtonBounds(i).contains(e.getPoint())) {
                        hoveredIndex = i;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        break;
                    }
                }

                if (hoveredIndex == -1) {
                    setCursor(Cursor.getDefaultCursor());
                }

                // Only repaint if the hover state actually changed
                if (previousHover != hoveredIndex) {
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                for (int i = 0; i < buttonLabels.length; i++) {
                    if (getButtonBounds(i).contains(e.getPoint())) {
                        stopMusic();
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameMenu.this);
                        topFrame.getContentPane().removeAll();

                        if (i == 0) { // EASY
                            EasyGamePanel easy = new EasyGamePanel();
                            topFrame.add(easy);
                            easy.requestFocusInWindow();
                        } else if (i == 1) { // HARD
                            HardGamePanel hard = new HardGamePanel();
                            topFrame.add(hard);
                            hard.requestFocusInWindow();
                        }
                        
                        topFrame.revalidate();
                        topFrame.repaint();
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (background != null) g2d.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        int logoW = (int) (getWidth() * 0.65);
        int logoH = (int) (logoW * 0.5);
        int logoX = (getWidth() - logoW) / 2;
        int logoY = (getHeight() - logoH) / 2;

        if (logo != null) g2d.drawImage(logo, logoX, logoY, logoW, logoH, this);

        for (int i = 0; i < buttonLabels.length; i++) {
            // Pass the hover state to the drawing method
            drawProfessionalText(g2d, getButtonBounds(i), buttonLabels[i], i == hoveredIndex);
        }
    }

    private Rectangle getButtonBounds(int index) {
        int logoW = (int) (getWidth() * 0.65);
        int logoH = (int) (logoW * 0.5);
        int logoY = (getHeight() - logoH) / 2;
        int btnW = logoW / 2, btnH = logoH / 12, spacing = 10;
        int totalHeight = (btnH * 3) + (spacing * 2);
        int startY = (logoY + logoH) - totalHeight - (int)(logoH * 0.15);
        return new Rectangle((getWidth() - btnW) / 2, startY + (index * (btnH + spacing)), btnW, btnH);
    }

    private void drawProfessionalText(Graphics2D g2d, Rectangle rect, String text, boolean isHovered) {
        // Change font size or style if hovered
        int fontSize = isHovered ? (int)(rect.height * 0.85) : (int)(rect.height * 0.75);
        g2d.setFont(new Font("Century Gothic", isHovered ? Font.BOLD : Font.PLAIN, fontSize));
        
        int tx = rect.x + (rect.width - g2d.getFontMetrics().stringWidth(text)) / 2;
        int ty = rect.y + (rect.height + g2d.getFontMetrics().getAscent()) / 2 - 2;

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, tx + 1, ty + 1);

        // Main Text Color (Brighten if hovered)
        if (isHovered) {
            g2d.setColor(Color.WHITE); // Highlight color
        } else {
            g2d.setColor(new Color(230, 210, 230)); // Default Light Plum
        }
        
        g2d.drawString(text, tx, ty);
    }
}