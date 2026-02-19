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
    private final String[] buttonLabels = { "EASY MODE", "HARD MODE", "LEADERBOARD" };
    private int hoveredIndex = -1; 

    private Clip menuMusicClip;

    private LeaderboardManager manager;

    private void playSound(String filePath, boolean loop) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
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
            menuMusicClip = null; 
        }
    }

    public GameMenu() {
        this(new LeaderboardManager());

    }
    public GameMenu(LeaderboardManager manager) {
        this.manager = manager;
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

                        if (i == 2) { 
                            new LeaderboardUI(manager);
                            return;
                        }

                        stopMusic();
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameMenu.this);
                        topFrame.getContentPane().removeAll();

                        if (i == 0) {
                            EasyGamePanel easy = new EasyGamePanel(manager);
                            topFrame.add(easy);
                            easy.requestFocusInWindow();
                        } else if (i == 1) {
                            HardGamePanel hard = new HardGamePanel(manager);
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

        if (background != null)
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        int logoW = (int) (getWidth() * 0.65);
        int logoH = (int) (logoW * 0.5);
        int logoX = (getWidth() - logoW) / 2;
        int logoY = (getHeight() - logoH) / 2;

        if (logo != null)
            g2d.drawImage(logo, logoX, logoY, logoW, logoH, this);

        for (int i = 0; i < buttonLabels.length; i++) {
            drawProfessionalText(g2d, getButtonBounds(i), buttonLabels[i], i == hoveredIndex);
        }
    }

    private Rectangle getButtonBounds(int index) {
        int logoW = (int) (getWidth() * 0.65);
        int logoH = (int) (logoW * 0.5);
        int logoY = (getHeight() - logoH) / 2;
        int btnW = logoW / 2, btnH = logoH / 12, spacing = 10;
        int totalHeight = (btnH * 3) + (spacing * 2);
        int startY = (logoY + logoH) - totalHeight - (int) (logoH * 0.15);
        return new Rectangle((getWidth() - btnW) / 2, startY + (index * (btnH + spacing)), btnW, btnH);
    }

    private void drawProfessionalText(Graphics2D g2d, Rectangle rect, String text, boolean isHovered) {
     
        int fontSize = isHovered ? (int) (rect.height * 0.85) : (int) (rect.height * 0.75);
        g2d.setFont(new Font("Century Gothic", isHovered ? Font.BOLD : Font.PLAIN, fontSize));

        int tx = rect.x + (rect.width - g2d.getFontMetrics().stringWidth(text)) / 2;
        int ty = rect.y + (rect.height + g2d.getFontMetrics().getAscent()) / 2 - 2;

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, tx + 1, ty + 1);
        
        if (isHovered) {
            g2d.setColor(Color.WHITE); 
        } else {
            g2d.setColor(new Color(230, 210, 230));
        }

        g2d.drawString(text, tx, ty);
    }
}