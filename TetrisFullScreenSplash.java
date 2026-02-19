import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class TetrisFullScreenSplash extends JWindow {
    private float opacity = 0f;
    private BufferedImage backgroundImage;
    private float scale = 1.0f;
    private boolean growing = true;
    private boolean fadingOut = false;

    private Clip menuMusicClip;

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

    public TetrisFullScreenSplash(String imagePath) {
        try {
            backgroundImage = ImageIO.read(new File(imagePath));
        } catch (Exception e) {
            System.out.println("Error: Image not found. Jumping to Menu.");
            launchGameMenu();
            return;
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screenSize.width, screenSize.height);
        setBackground(new Color(0, 0, 0, 0));
        setOpacity(0f);
        setAlwaysOnTop(true);
        playSound("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\sounds\\splash.wav", true);

        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage == null)
                    return;
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                double imgAspect = (double) backgroundImage.getWidth() / backgroundImage.getHeight();
                double screenAspect = (double) w / h;

                int drawW = w, drawH = h;
                if (screenAspect > imgAspect) {
                    drawH = (int) (w / imgAspect);
                } else {
                    drawW = (int) (h * imgAspect);
                }

                int finalW = (int) (drawW * scale);
                int finalH = (int) (drawH * scale);
                int x = (w - finalW) / 2;
                int y = (h - finalH) / 2;

                g2d.drawImage(backgroundImage, x, y, finalW, finalH, null);
                g2d.dispose();
            }
        };

        contentPane.setOpaque(false);
        setContentPane(contentPane);
        setVisible(true);

        startAnimations();
    }

    private void startAnimations() {
        // Animation Loop
        Timer animTimer = new Timer(20, e -> {
            // Fade logic
            if (!fadingOut) {
                if (opacity < 1.0f) {
                    opacity += 0.02f;
                    if (opacity > 1.0f)
                        opacity = 1.0f;
                    setOpacity(opacity);
                }
            } else {
                if (opacity > 0.0f) {
                    opacity -= 0.05f;
                    if (opacity < 0.0f)
                        opacity = 0.0f;
                    setOpacity(opacity);
                }
            }

            // Pulse logic
            if (growing) {
                scale += 0.0005f;
                if (scale >= 1.03f)
                    growing = false;
            } else {
                scale -= 0.0005f;
                if (scale <= 1.0f)
                    growing = true;
            }
            repaint();
        });
        animTimer.start();

        // Sequence: Display for 10seconds -> Fade Out -> Open Menu
        Timer mainTimer = new Timer(10000, e -> {
            fadingOut = true;

            // Give it 500ms to finish fading out before switching
            Timer switchTimer = new Timer(500, ev -> {
                animTimer.stop();
                this.dispose(); // Close splash
                stopMusic();
                launchGameMenu(); // Open your class
            });
            switchTimer.setRepeats(false);
            switchTimer.start();
        });
        mainTimer.setRepeats(false);
        mainTimer.start();
    }

    private void launchGameMenu() {
        SwingUtilities.invokeLater(() -> {
            // 1. Create the 'Wall' (Window)
            JFrame frame = new JFrame("Tetris");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 2. Add your 'Poster' (The GameMenu JPanel)
            GameMenu menu = new GameMenu();
            frame.add(menu);

            // 3. Size and Show
            frame.setSize(800, 700);
            frame.setLocationRelativeTo(null); // Center it
            frame.setVisible(true);

            System.out.println("Menu Window Created and Panel Added!");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TetrisFullScreenSplash("D:\\BS-CS\\BS-CS-3rd-Semester\\interface\\src\\images\\background.png"));
    }
}