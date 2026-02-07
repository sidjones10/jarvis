package com.jarvis.ui;

import com.jarvis.core.JarvisEngine;
import com.jarvis.core.ResponseListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JarvisUI extends JFrame implements ResponseListener {

    private static final Color BG_DARK = new Color(18, 18, 24);
    private static final Color BG_PANEL = new Color(25, 25, 35);
    private static final Color BG_INPUT = new Color(35, 35, 50);
    private static final Color ACCENT_BLUE = new Color(0, 170, 255);
    private static final Color ACCENT_CYAN = new Color(0, 230, 230);
    private static final Color TEXT_PRIMARY = new Color(220, 225, 235);
    private static final Color TEXT_SECONDARY = new Color(130, 140, 160);
    private static final Color USER_COLOR = new Color(100, 180, 255);
    private static final Color JARVIS_COLOR = new Color(0, 230, 200);
    private static final Color BORDER_COLOR = new Color(45, 50, 70);

    private final JarvisEngine engine;
    private JTextPane chatPane;
    private JTextField inputField;
    private StyledDocument doc;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private JPanel arcReactorPanel;
    private float pulsePhase = 0f;
    private Timer pulseTimer;

    public JarvisUI(JarvisEngine engine) {
        this.engine = engine;
        initUI();
        startPulseAnimation();
    }

    private void initUI() {
        setTitle("J.A.R.V.I.S. — Just A Rather Very Intelligent System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(600, 450));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(createHeader(), BorderLayout.NORTH);
        add(createChatArea(), BorderLayout.CENTER);
        add(createInputArea(), BorderLayout.SOUTH);

        appendJarvisMessage("J.A.R.V.I.S. online.\nGood " + getTimeOfDay() + ", sir. How may I assist you?\n\nType 'help' to see available commands.");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 20, 12, 20)
        ));

        // Arc reactor indicator
        arcReactorPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                float glow = (float) (0.5 + 0.5 * Math.sin(pulsePhase));
                Color glowColor = new Color(
                    ACCENT_CYAN.getRed(),
                    ACCENT_CYAN.getGreen(),
                    ACCENT_CYAN.getBlue(),
                    (int) (80 + 120 * glow)
                );

                // Outer glow
                g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 40));
                g2.fillOval(x - 3, y - 3, size + 6, size + 6);

                // Main circle
                g2.setColor(glowColor);
                g2.fillOval(x, y, size, size);

                // Inner bright center
                g2.setColor(new Color(200, 255, 255, (int) (150 + 105 * glow)));
                g2.fillOval(x + size / 4, y + size / 4, size / 2, size / 2);

                g2.dispose();
            }
        };
        arcReactorPanel.setOpaque(false);
        arcReactorPanel.setPreferredSize(new Dimension(28, 28));

        JLabel titleLabel = new JLabel("  J.A.R.V.I.S.");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_CYAN);

        JLabel subtitleLabel = new JLabel("Just A Rather Very Intelligent System");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(arcReactorPanel);
        left.add(titleLabel);

        header.add(left, BorderLayout.WEST);
        header.add(subtitleLabel, BorderLayout.EAST);

        return header;
    }

    private JScrollPane createChatArea() {
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_DARK);
        chatPane.setForeground(TEXT_PRIMARY);
        chatPane.setFont(new Font("Monospaced", Font.PLAIN, 13));
        chatPane.setBorder(new EmptyBorder(15, 20, 15, 20));
        chatPane.setCaretColor(ACCENT_BLUE);
        doc = chatPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Style the scrollbar
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_DARK;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        return scrollPane;
    }

    private JPanel createInputArea() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG_PANEL);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel promptLabel = new JLabel(">");
        promptLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        promptLabel.setForeground(ACCENT_BLUE);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(TEXT_PRIMARY);
        inputField.setCaretColor(ACCENT_BLUE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        inputField.addActionListener(e -> handleInput());

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    navigateHistory(-1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    navigateHistory(1);
                }
            }
        });

        JButton sendButton = new JButton("SEND") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_BLUE.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_BLUE.brighter());
                } else {
                    g2.setColor(ACCENT_BLUE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.setPreferredSize(new Dimension(80, 36));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> handleInput());

        inputPanel.add(promptLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        return inputPanel;
    }

    private void handleInput() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        commandHistory.add(text);
        historyIndex = commandHistory.size();
        inputField.setText("");

        appendUserMessage(text);

        if (text.equalsIgnoreCase("clear")) {
            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException ignored) {}
            appendJarvisMessage("Display cleared, sir.");
            return;
        }

        if (text.equalsIgnoreCase("exit") || text.equalsIgnoreCase("quit") || text.equalsIgnoreCase("goodbye")) {
            appendJarvisMessage("Powering down. Until next time, sir.");
            Timer exitTimer = new Timer(1500, e -> System.exit(0));
            exitTimer.setRepeats(false);
            exitTimer.start();
            return;
        }

        String response = engine.processInput(text);
        appendJarvisMessage(response);
    }

    private void appendUserMessage(String text) {
        appendStyledText("\n You  ", createStyle(USER_COLOR, true));
        appendStyledText(text + "\n", createStyle(TEXT_PRIMARY, false));
    }

    private void appendJarvisMessage(String text) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        appendStyledText("\n JARVIS [" + timestamp + "]  ", createStyle(JARVIS_COLOR, true));
        appendStyledText(text + "\n", createStyle(TEXT_PRIMARY, false));
        chatPane.setCaretPosition(doc.getLength());
    }

    private void appendStyledText(String text, SimpleAttributeSet style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ignored) {}
    }

    private SimpleAttributeSet createStyle(Color color, boolean bold) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 13);
        return style;
    }

    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) return;
        historyIndex += direction;
        if (historyIndex < 0) historyIndex = 0;
        if (historyIndex >= commandHistory.size()) {
            historyIndex = commandHistory.size();
            inputField.setText("");
            return;
        }
        inputField.setText(commandHistory.get(historyIndex));
    }

    private void startPulseAnimation() {
        pulseTimer = new Timer(50, e -> {
            pulsePhase += 0.08f;
            if (pulsePhase > Math.PI * 2) pulsePhase -= Math.PI * 2;
            arcReactorPanel.repaint();
        });
        pulseTimer.start();
    }

    private String getTimeOfDay() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "morning";
        if (hour < 17) return "afternoon";
        return "evening";
    }

    @Override
    public void onResponse(String response) {
        SwingUtilities.invokeLater(() -> appendJarvisMessage(response));
    }
}
