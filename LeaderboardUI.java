import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LeaderboardUI extends JFrame {
    private final LeaderboardManager manager;
    private JPanel listPanel;

    // ====== CHANGE THIS PATH ======
    private static final String ICON_PATH = "C:\\xampp\\htdocs\\Tetris\\src\\images\\icon.png";

    static class Entry {
        String name;
        int score;

        Entry(String n, int s) {
            name = n;
            score = s;
        }
    }

    public LeaderboardUI(LeaderboardManager manager) {
        this.manager = manager;
        setTitle("Leaderboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 520);
        setLocationRelativeTo(null);

        // ===== Background =====
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(235, 239, 245));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(210, 220, 235));
                Polygon p1 = new Polygon();
                p1.addPoint(0, 0);
                p1.addPoint(220, 0);
                p1.addPoint(0, 220);
                g2.fillPolygon(p1);

                g2.setColor(new Color(215, 225, 240));
                Polygon p2 = new Polygon();
                p2.addPoint(getWidth(), getHeight());
                p2.addPoint(getWidth() - 260, getHeight());
                p2.addPoint(getWidth(), getHeight() - 260);
                g2.fillPolygon(p2);

                g2.dispose();
            }
        };
        setContentPane(root);
    
        JPanel board = new RoundedPanel(26, new Color(12, 45, 95));
        board.setPreferredSize(new Dimension(480, 380));
        board.setLayout(new BorderLayout());
        board.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        top.add(title, BorderLayout.CENTER);

        board.add(top, BorderLayout.NORTH);

        this.listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(this.listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(14, 0, 0, 0));


        JScrollPane scroll = new JScrollPane(this.listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scroll.setPreferredSize(new Dimension(445, 260));

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        centerWrap.setOpaque(false);
        centerWrap.add(scroll);

        JButton backToMenu = new JButton("Back");
        backToMenu.setFocusPainted(false);
        backToMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToMenu.setFont(new Font("Arial", Font.BOLD, 14));
        backToMenu.setBackground(new Color(35, 95, 170));
        backToMenu.setForeground(Color.WHITE);
        backToMenu.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        backToMenu.addActionListener(e -> dispose());

        JPanel bottomWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        bottomWrap.setOpaque(false);
        bottomWrap.add(backToMenu);

        JPanel midAndBottom = new JPanel();
        midAndBottom.setOpaque(false);
        midAndBottom.setLayout(new BorderLayout());
        midAndBottom.add(centerWrap, BorderLayout.CENTER);
        midAndBottom.add(bottomWrap, BorderLayout.SOUTH);

        board.add(midAndBottom, BorderLayout.CENTER);

        final Dimension normalBoardSize = new Dimension(480, 380);
        final Dimension normalScrollSize = new Dimension(445, 260);

        Runnable applySizeMode = () -> {
            boolean maximized = (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;

            if (maximized) {
                int fullListHeight = listPanel.getPreferredSize().height;

                int desiredBoardHeight = Math.max(380, fullListHeight + 170);
                board.setPreferredSize(new Dimension(480, desiredBoardHeight));

                scroll.setPreferredSize(new Dimension(445, fullListHeight));
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            } else {
                board.setPreferredSize(normalBoardSize);
                scroll.setPreferredSize(normalScrollSize);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            }

            board.revalidate();
            board.repaint();
            scroll.revalidate();
            scroll.repaint();
        };

        addWindowStateListener(e -> SwingUtilities.invokeLater(applySizeMode));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(applySizeMode);
            }
        });
        SwingUtilities.invokeLater(applySizeMode);

        root.add(board, new GridBagConstraints());
        refreshLeaderboardUI();
        setVisible(true);
    }

    private JPanel createRow(int rank, Entry entry) {
        JPanel row = new RoundedPanel(18, new Color(35, 95, 170));
        row.setLayout(new BorderLayout());
        row.setMaximumSize(new Dimension(420, 42));
        row.setPreferredSize(new Dimension(420, 42));
        row.setBorder(new EmptyBorder(6, 12, 6, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel rankLbl = new JLabel(String.valueOf(rank));
        rankLbl.setForeground(Color.WHITE);
        rankLbl.setFont(new Font("Arial", Font.BOLD, 16));
        rankLbl.setPreferredSize(new Dimension(18, 26));

        JLabel iconLbl = loadIconLabel();

        JLabel nameLbl = new JLabel(entry.name);
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(new Font("Arial", Font.PLAIN, 16));

        left.add(rankLbl);
        left.add(iconLbl);
        left.add(nameLbl);

        JLabel scoreLbl = new JLabel(String.valueOf(entry.score));
        scoreLbl.setForeground(Color.WHITE);
        scoreLbl.setFont(new Font("Arial", Font.BOLD, 16));

        row.add(left, BorderLayout.WEST);
        row.add(scoreLbl, BorderLayout.EAST);

        return row;
    }

    private JLabel loadIconLabel() {
        ImageIcon icon = new ImageIcon(ICON_PATH);
        if (icon.getIconWidth() <= 0) {
            return new JLabel(new CircleIcon(28, new Color(255, 255, 255, 220)));
        }
        Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(img));
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class CircleIcon implements Icon {
        private final int size;
        private final Color color;

        CircleIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawOval(x, y, size, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }
    }
    private void refreshLeaderboardUI() {
    listPanel.removeAll();

    MyMap<Integer, Integer> map = manager.getLeaderboard();
    Node<Integer, Integer> cur = map.getHead();

    int rank = 1;

    while (cur != null && rank <= 10) {
        int gameId = cur.key;
        int score = cur.value;

        Entry e = new Entry("Game " + gameId, score); 
        JPanel row = createRow(rank, e);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(row);

        if (rank != 10) listPanel.add(Box.createVerticalStrut(10));

        cur = cur.next;
        rank++;
    }

    while (rank <= 10) {
        Entry e = new Entry("---", 0);
        JPanel row = createRow(rank, e);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(row);

        if (rank != 10) listPanel.add(Box.createVerticalStrut(10));
        rank++;
    }

    listPanel.revalidate();
    listPanel.repaint();
}

}
