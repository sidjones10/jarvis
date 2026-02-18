package com.jarvis.ui;

import com.jarvis.commands.DraftCommand;
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
    private static final Color BG_CARD = new Color(30, 30, 42);
    private static final Color ACCENT_BLUE = new Color(0, 170, 255);
    private static final Color ACCENT_CYAN = new Color(0, 230, 230);
    private static final Color ACCENT_GREEN = new Color(0, 200, 120);
    private static final Color ACCENT_PURPLE = new Color(150, 100, 255);
    private static final Color TEXT_PRIMARY = new Color(220, 225, 235);
    private static final Color TEXT_SECONDARY = new Color(130, 140, 160);
    private static final Color USER_COLOR = new Color(100, 180, 255);
    private static final Color JARVIS_COLOR = new Color(0, 230, 200);
    private static final Color BORDER_COLOR = new Color(45, 50, 70);

    private static final String HOME_VIEW = "home";
    private static final String CHAT_VIEW = "chat";
    private static final String DISCOVER_VIEW = "discover";
    private static final String PROFILE_VIEW = "profile";

    private final JarvisEngine engine;
    private DraftCommand draftCommand;

    // Chat components
    private JTextPane chatPane;
    private JTextField inputField;
    private StyledDocument doc;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    // Header components
    private JPanel arcReactorPanel;
    private float pulsePhase = 0f;
    private Timer pulseTimer;

    // Navigation
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private JPanel discoverPanel;
    private JPanel profilePanel;
    private JPanel bottomBar;

    // Home page compose area
    private JTextArea homeComposeArea;

    // Profile page compose area
    private JTextArea profileComposeArea;

    // Nav buttons (to update active state)
    private JButton navHome;
    private JButton navChat;
    private JButton navDiscover;
    private JButton navProfile;

    public JarvisUI(JarvisEngine engine) {
        this.engine = engine;
        initUI();
        startPulseAnimation();
    }

    public void setDraftCommand(DraftCommand draftCommand) {
        this.draftCommand = draftCommand;
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

        // Center: CardLayout for switching pages
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBackground(BG_DARK);
        centerPanel.add(createHomePage(), HOME_VIEW);
        centerPanel.add(createChatPage(), CHAT_VIEW);
        discoverPanel = createDiscoverPage();
        centerPanel.add(discoverPanel, DISCOVER_VIEW);
        profilePanel = createProfilePage();
        centerPanel.add(profilePanel, PROFILE_VIEW);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom navigation bar
        bottomBar = createNavBar();
        add(bottomBar, BorderLayout.SOUTH);

        // Start on home page
        showView(HOME_VIEW);
    }

    // =========================================================================
    //  Header
    // =========================================================================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 20, 12, 20)
        ));

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
                    ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(), ACCENT_CYAN.getBlue(),
                    (int) (80 + 120 * glow));
                g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 40));
                g2.fillOval(x - 3, y - 3, size + 6, size + 6);
                g2.setColor(glowColor);
                g2.fillOval(x, y, size, size);
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

    // =========================================================================
    //  Bottom Navigation Bar
    // =========================================================================

    private JPanel createNavBar() {
        JPanel nav = new JPanel(new GridLayout(1, 4, 0, 0));
        nav.setBackground(BG_PANEL);
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        navHome = createNavButton("HOME", HOME_VIEW);
        navChat = createNavButton("CHAT", CHAT_VIEW);
        navDiscover = createNavButton("DISCOVER", DISCOVER_VIEW);
        navProfile = createNavButton("PROFILE", PROFILE_VIEW);

        nav.add(navHome);
        nav.add(navChat);
        nav.add(navDiscover);
        nav.add(navProfile);

        return nav;
    }

    private JButton createNavButton(String label, String viewName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(BG_PANEL);
        btn.setBorder(new EmptyBorder(12, 0, 12, 0));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showView(viewName));
        return btn;
    }

    private void showView(String viewName) {
        if (viewName.equals(DISCOVER_VIEW)) {
            refreshDiscoverPanel();
        }
        if (viewName.equals(PROFILE_VIEW)) {
            refreshProfileDraftList();
        }
        cardLayout.show(centerPanel, viewName);

        // Highlight active nav button
        navHome.setForeground(viewName.equals(HOME_VIEW) ? ACCENT_CYAN : TEXT_SECONDARY);
        navChat.setForeground(viewName.equals(CHAT_VIEW) ? ACCENT_CYAN : TEXT_SECONDARY);
        navDiscover.setForeground(viewName.equals(DISCOVER_VIEW) ? ACCENT_CYAN : TEXT_SECONDARY);
        navProfile.setForeground(viewName.equals(PROFILE_VIEW) ? ACCENT_CYAN : TEXT_SECONDARY);
    }

    // =========================================================================
    //  HOME PAGE — compose & save draft
    // =========================================================================

    private JPanel createHomePage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG_DARK);

        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(BG_DARK);
        welcomePanel.setBorder(new EmptyBorder(30, 40, 10, 40));

        JLabel welcomeLabel = new JLabel("Welcome back, sir.");
        welcomeLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        welcomeLabel.setForeground(ACCENT_CYAN);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Compose a new draft below.");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(subtitleLabel);
        page.add(welcomePanel, BorderLayout.NORTH);

        // Compose area
        JPanel composePanel = new JPanel(new BorderLayout(0, 12));
        composePanel.setBackground(BG_DARK);
        composePanel.setBorder(new EmptyBorder(10, 40, 30, 40));

        homeComposeArea = new JTextArea();
        homeComposeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        homeComposeArea.setBackground(BG_INPUT);
        homeComposeArea.setForeground(TEXT_PRIMARY);
        homeComposeArea.setCaretColor(ACCENT_BLUE);
        homeComposeArea.setLineWrap(true);
        homeComposeArea.setWrapStyleWord(true);
        homeComposeArea.setBorder(new EmptyBorder(14, 14, 14, 14));

        JScrollPane composeScroll = new JScrollPane(homeComposeArea);
        composeScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        composeScroll.getViewport().setBackground(BG_INPUT);
        styleScrollBar(composeScroll);

        JButton saveDraftBtn = createStyledButton("SAVE DRAFT", ACCENT_GREEN, 140);
        saveDraftBtn.setPreferredSize(new Dimension(140, 42));
        saveDraftBtn.addActionListener(e -> saveDraftFrom(homeComposeArea));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(saveDraftBtn);

        composePanel.add(composeScroll, BorderLayout.CENTER);
        composePanel.add(btnRow, BorderLayout.SOUTH);

        page.add(composePanel, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    //  CHAT PAGE
    // =========================================================================

    private JPanel createChatPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG_DARK);
        page.add(createChatArea(), BorderLayout.CENTER);
        page.add(createChatInputArea(), BorderLayout.SOUTH);
        return page;
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
        styleScrollBar(scrollPane);

        appendJarvisMessage("J.A.R.V.I.S. online.\nGood " + getTimeOfDay() + ", sir. How may I assist you?\n\nType 'help' to see available commands.");

        return scrollPane;
    }

    private JPanel createChatInputArea() {
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
                if (e.getKeyCode() == KeyEvent.VK_UP) navigateHistory(-1);
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) navigateHistory(1);
            }
        });

        JButton sendButton = createStyledButton("SEND", ACCENT_BLUE, 80);
        sendButton.addActionListener(e -> handleInput());

        inputPanel.add(promptLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        return inputPanel;
    }

    // =========================================================================
    //  DISCOVER PAGE — view all saved drafts
    // =========================================================================

    private JPanel createDiscoverPage() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_DARK);
        topBar.setBorder(new EmptyBorder(24, 40, 10, 40));

        JLabel discoverTitle = new JLabel("Discover — Saved Drafts");
        discoverTitle.setFont(new Font("Monospaced", Font.BOLD, 20));
        discoverTitle.setForeground(ACCENT_CYAN);

        topBar.add(discoverTitle, BorderLayout.WEST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // Placeholder; rebuilt on each visit
        JPanel cardsPlaceholder = new JPanel();
        cardsPlaceholder.setBackground(BG_DARK);
        JScrollPane scroll = new JScrollPane(cardsPlaceholder);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_DARK);
        styleScrollBar(scroll);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void refreshDiscoverPanel() {
        BorderLayout layout = (BorderLayout) discoverPanel.getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp != null) {
            discoverPanel.remove(centerComp);
        }

        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(BG_DARK);
        cardsContainer.setBorder(new EmptyBorder(10, 40, 30, 40));

        if (draftCommand == null) {
            addPlaceholderLabel(cardsContainer, "Draft system not available.");
        } else {
            List<String> names = draftCommand.listDraftNames();
            if (names.isEmpty()) {
                addPlaceholderLabel(cardsContainer, "No drafts saved yet. Go to Home or Profile to create one.");
            } else {
                for (String name : names) {
                    cardsContainer.add(createDraftCard(name));
                    cardsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(scroll);

        discoverPanel.add(scroll, BorderLayout.CENTER);
        discoverPanel.revalidate();
        discoverPanel.repaint();
    }

    private JPanel createDraftCard(String name) {
        String content = draftCommand.readDraftContent(name);
        String preview = content != null
                ? (content.length() > 150 ? content.substring(0, 150) + "..." : content)
                : "(unable to read)";

        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        nameLabel.setForeground(ACCENT_CYAN);

        JLabel previewLabel = new JLabel("<html><body style='width:450px'>" + escapeHtml(preview) + "</body></html>");
        previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        previewLabel.setForeground(TEXT_SECONDARY);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);

        JButton viewBtn = createStyledButton("VIEW", ACCENT_BLUE, 65);
        viewBtn.addActionListener(e -> {
            showView(CHAT_VIEW);
            appendJarvisMessage("Draft \"" + name + "\":\n\n" + (content != null ? content : "(empty)"));
        });

        JButton deleteBtn = createStyledButton("DELETE", new Color(200, 60, 60), 75);
        deleteBtn.addActionListener(e -> {
            draftCommand.deleteDraftFile(name);
            refreshDiscoverPanel();
        });

        buttons.add(viewBtn);
        buttons.add(deleteBtn);

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(previewLabel, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.EAST);

        return card;
    }

    // =========================================================================
    //  PROFILE PAGE — user info + draft management + create draft
    // =========================================================================

    private JPanel createProfilePage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG_DARK);

        // Profile header
        JPanel profileHeader = new JPanel();
        profileHeader.setLayout(new BoxLayout(profileHeader, BoxLayout.Y_AXIS));
        profileHeader.setBackground(BG_DARK);
        profileHeader.setBorder(new EmptyBorder(24, 40, 10, 40));

        JLabel profileTitle = new JLabel("Profile");
        profileTitle.setFont(new Font("Monospaced", Font.BOLD, 20));
        profileTitle.setForeground(ACCENT_PURPLE);
        profileTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userName = new JLabel("User: " + System.getProperty("user.name", "Unknown"));
        userName.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userName.setForeground(TEXT_SECONDARY);
        userName.setAlignmentX(Component.LEFT_ALIGNMENT);
        userName.setBorder(new EmptyBorder(4, 0, 0, 0));

        profileHeader.add(profileTitle);
        profileHeader.add(userName);
        page.add(profileHeader, BorderLayout.NORTH);

        // Center: compose area + draft list
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(BG_DARK);
        centerContent.setBorder(new EmptyBorder(10, 40, 30, 40));

        // -- Compose section --
        JLabel composeLabel = new JLabel("Create a new draft:");
        composeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        composeLabel.setForeground(TEXT_PRIMARY);
        composeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileComposeArea = new JTextArea(4, 40);
        profileComposeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        profileComposeArea.setBackground(BG_INPUT);
        profileComposeArea.setForeground(TEXT_PRIMARY);
        profileComposeArea.setCaretColor(ACCENT_BLUE);
        profileComposeArea.setLineWrap(true);
        profileComposeArea.setWrapStyleWord(true);
        profileComposeArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane composeScroll = new JScrollPane(profileComposeArea);
        composeScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        composeScroll.getViewport().setBackground(BG_INPUT);
        composeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        composeScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleScrollBar(composeScroll);

        JButton saveBtn = createStyledButton("SAVE DRAFT", ACCENT_GREEN, 140);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveDraftFrom(profileComposeArea));

        JPanel saveBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        saveBtnRow.setOpaque(false);
        saveBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtnRow.add(saveBtn);

        // -- Draft list section --
        JLabel myDraftsLabel = new JLabel("My Drafts:");
        myDraftsLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        myDraftsLabel.setForeground(TEXT_PRIMARY);
        myDraftsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        myDraftsLabel.setBorder(new EmptyBorder(20, 0, 6, 0));

        centerContent.add(composeLabel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 8)));
        centerContent.add(composeScroll);
        centerContent.add(Box.createRigidArea(new Dimension(0, 10)));
        centerContent.add(saveBtnRow);
        centerContent.add(myDraftsLabel);
        // Draft names will be appended in refreshProfileDraftList()

        JScrollPane centerScroll = new JScrollPane(centerContent);
        centerScroll.setBorder(BorderFactory.createEmptyBorder());
        centerScroll.getViewport().setBackground(BG_DARK);
        centerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(centerScroll);

        page.add(centerScroll, BorderLayout.CENTER);
        return page;
    }

    private void refreshProfileDraftList() {
        // Find the center content panel (inside the JScrollPane -> JViewport)
        BorderLayout pageLayout = (BorderLayout) profilePanel.getLayout();
        Component centerComp = pageLayout.getLayoutComponent(BorderLayout.CENTER);
        if (!(centerComp instanceof JScrollPane)) return;
        JScrollPane scroll = (JScrollPane) centerComp;
        Component view = scroll.getViewport().getView();
        if (!(view instanceof JPanel)) return;
        JPanel centerContent = (JPanel) view;

        // Remove old draft name labels (everything after index 6: composeLabel, spacer, composeScroll, spacer, saveBtnRow, myDraftsLabel)
        while (centerContent.getComponentCount() > 6) {
            centerContent.remove(centerContent.getComponentCount() - 1);
        }

        if (draftCommand != null) {
            List<String> names = draftCommand.listDraftNames();
            if (names.isEmpty()) {
                JLabel empty = new JLabel("No drafts yet.");
                empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
                empty.setForeground(TEXT_SECONDARY);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                centerContent.add(empty);
            } else {
                for (String name : names) {
                    JLabel draftLabel = new JLabel("  - " + name);
                    draftLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
                    draftLabel.setForeground(ACCENT_CYAN);
                    draftLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    draftLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
                    centerContent.add(draftLabel);
                }
            }
        }

        centerContent.revalidate();
        centerContent.repaint();
    }

    // =========================================================================
    //  Draft saving — shared by Home & Profile
    // =========================================================================

    private void saveDraftFrom(JTextArea composeArea) {
        String text = composeArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please type something before saving.",
                    "Empty Draft", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (draftCommand == null) {
            JOptionPane.showMessageDialog(this,
                    "Draft system is not available.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = JOptionPane.showInputDialog(
                this,
                "Enter a name for this draft:",
                "Save Draft",
                JOptionPane.PLAIN_MESSAGE
        );

        if (name == null || name.trim().isEmpty()) {
            return; // User cancelled
        }

        boolean saved = draftCommand.saveDraftFile(name.trim(), text);
        if (saved) {
            composeArea.setText("");
            showView(DISCOVER_VIEW);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error saving draft. Please try again.",
                    "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    //  Chat handling
    // =========================================================================

    private void handleInput() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        commandHistory.add(text);
        historyIndex = commandHistory.size();
        inputField.setText("");

        appendUserMessage(text);

        if (text.equalsIgnoreCase("clear")) {
            try { doc.remove(0, doc.getLength()); }
            catch (BadLocationException ignored) {}
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
        try { doc.insertString(doc.getLength(), text, style); }
        catch (BadLocationException ignored) {}
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

    // =========================================================================
    //  Utilities
    // =========================================================================

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

    private JButton createStyledButton(String label, Color color, int width) {
        JButton button = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(color.darker());
                else if (getModel().isRollover()) g2.setColor(color.brighter());
                else g2.setColor(color);
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
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, 36));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    private void addPlaceholderLabel(JPanel container, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("Monospaced", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(label);
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public void onResponse(String response) {
        SwingUtilities.invokeLater(() -> appendJarvisMessage(response));
    }
}
