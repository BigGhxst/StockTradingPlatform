import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class StockTradingPlatformGUI extends JFrame {

    private StockMarket stockMarket;
    private StockDatabase db;
    private Map<String, User> users;
    private Map<String, Portfolio> portfolios;
    private List<Transaction> transactions;

    private User currentUser;
    private Timer marketTimer;

    private int transactionCounter = 1000;

    private JTabbedPane tabbedPane;
    private JTable marketTable;
    private JTable portfolioTable;
    private JTable transactionTable;
    private DefaultTableModel marketTableModel;
    private DefaultTableModel portfolioTableModel;
    private DefaultTableModel transactionTableModel;

    private JLabel userLabel;
    private JLabel balanceLabel;
    private JLabel portfolioValueLabel;
    private JLabel profitLossLabel;
    private JLabel marketStatusLabel;

    private JComboBox<String> stockCombo;
    private JTextField sharesField;
    private JButton buyButton;
    private JButton sellButton;

    private DecimalFormat df = new DecimalFormat("#0.00");

    // Login components
    private JFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton togglePasswordBtn;
    private boolean passwordVisible = false;

    public StockTradingPlatformGUI() {
        stockMarket = new StockMarket();
        db = new StockDatabase();
        users = new HashMap<>();
        portfolios = new HashMap<>();
        transactions = new ArrayList<>();

        loadAllData();

        if (users.isEmpty()) {
            initializeDefaultUsers();
        }

        showLoginScreen();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new StockTradingPlatformGUI());
    }

    // ===== LOGIN SCREEN =====

    private void showLoginScreen() {
        loginFrame = new JFrame("Login - Stock Trading Platform");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(420, 400);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(240, 244, 248));

        // Header with icon
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 244, 248));

        JLabel iconLabel = new JLabel("📈");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel headerLabel = new JLabel("Stock Trading Platform");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(new Color(44, 62, 80));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(iconLabel, BorderLayout.NORTH);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 244, 248));
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(new Color(44, 62, 80));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 35));
        usernameField.setBackground(Color.WHITE);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(new Color(44, 62, 80));
        formPanel.add(passLabel, gbc);

        // Password panel with toggle button
        JPanel passwordPanel = new JPanel(new BorderLayout(0, 0));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 0)
        ));
        passwordPanel.setPreferredSize(new Dimension(200, 35));

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(null);
        passwordField.setBackground(Color.WHITE);
        passwordField.setEchoChar('*');  // Simple asterisk
        passwordField.setPreferredSize(new Dimension(160, 35));
        passwordField.addActionListener(e -> attemptLogin());
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        togglePasswordBtn = new JButton();
        togglePasswordBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        togglePasswordBtn.setText("👁");
        togglePasswordBtn.setPreferredSize(new Dimension(40, 35));
        togglePasswordBtn.setBackground(Color.WHITE);
        togglePasswordBtn.setBorder(null);
        togglePasswordBtn.setFocusPainted(false);
        togglePasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordBtn.setToolTipText("Show Password");
        togglePasswordBtn.addActionListener(e -> togglePasswordVisibility());
        passwordPanel.add(togglePasswordBtn, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(passwordPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        infoPanel.setBackground(new Color(240, 244, 248));
        JLabel infoLabel = new JLabel("Default users: admin / trader / demo");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 244, 248));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton loginButton = createLoginButton("Login", new Color(46, 204, 113));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.addActionListener(e -> attemptLogin());

        JButton registerButton = createLoginButton("Register", new Color(52, 152, 219));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.addActionListener(e -> showRegisterDialog());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loginFrame.add(mainPanel);
        loginFrame.setVisible(true);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
            togglePasswordBtn.setText("🙈");
            togglePasswordBtn.setToolTipText("Hide Password");
        } else {
            passwordField.setEchoChar('*');
            togglePasswordBtn.setText("👁");
            togglePasswordBtn.setToolTipText("Show Password");
        }

        passwordField.requestFocus();
    }

    private JButton createLoginButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame,
                    "Please enter both username and password!",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
            loginFrame.dispose();
            currentUser = users.get(username);
            initializeMainApplication();
        } else {
            JOptionPane.showMessageDialog(loginFrame,
                    "Invalid username or password!\n\nAvailable users:\n  • admin (admin123)\n  • trader (trader123)\n  • demo (demo123)",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterDialog() {
        JDialog registerDialog = new JDialog(loginFrame, "Create New Account", true);
        registerDialog.setSize(420, 420);
        registerDialog.setLocationRelativeTo(loginFrame);
        registerDialog.setResizable(false);
        registerDialog.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Header
        JLabel headerLabel = new JLabel("Create New Account");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(44, 62, 80));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(headerLabel, gbc);

        gbc.insets = new Insets(6, 6, 6, 6);

        // Username
        JTextField newUsernameField = new JTextField(15);
        newUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newUsernameField.setPreferredSize(new Dimension(200, 35));
        newUsernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);
        JLabel userLbl = new JLabel("Username *");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLbl.setForeground(new Color(44, 62, 80));
        panel.add(userLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(newUsernameField, gbc);

        // Password
        JPasswordField newPasswordField = new JPasswordField(15);
        newPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newPasswordField.setPreferredSize(new Dimension(200, 35));
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        newPasswordField.setEchoChar('*');

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passLbl = new JLabel("Password *");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLbl.setForeground(new Color(44, 62, 80));
        panel.add(passLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(newPasswordField, gbc);

        // Full Name
        JTextField fullNameField = new JTextField(15);
        fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fullNameField.setPreferredSize(new Dimension(200, 35));
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel nameLbl = new JLabel("Full Name *");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(new Color(44, 62, 80));
        panel.add(nameLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(fullNameField, gbc);

        // Email
        JTextField emailField = new JTextField(15);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(200, 35));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel emailLbl = new JLabel("Email *");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailLbl.setForeground(new Color(44, 62, 80));
        panel.add(emailLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(emailField, gbc);

        // Note
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        JLabel noteLabel = new JLabel("* All fields are required");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(Color.GRAY);
        panel.add(noteLabel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton createBtn = createLoginButton("Create Account", new Color(46, 204, 113));
        createBtn.setPreferredSize(new Dimension(130, 38));

        JButton cancelBtn = createLoginButton("Cancel", new Color(149, 165, 166));
        cancelBtn.setPreferredSize(new Dimension(100, 38));

        createBtn.addActionListener(e -> {
            String newUsername = newUsernameField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();

            if (newUsername.isEmpty() || newPassword.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Please fill all required fields (*)!");
                return;
            }

            if (users.containsKey(newUsername)) {
                JOptionPane.showMessageDialog(registerDialog, "Username already exists!\nPlease choose another username.");
                return;
            }

            String userId = "U" + String.format("%03d", users.size() + 1);
            User newUser = new User(userId, newUsername, newPassword, fullName, email);
            users.put(newUsername, newUser);
            db.saveUser(newUser);

            Portfolio portfolio = new Portfolio(userId);
            portfolios.put(userId, portfolio);
            db.savePortfolio(portfolio);

            JOptionPane.showMessageDialog(registerDialog,
                    "Account created successfully!\n\nYou can now login with:\nUsername: " + newUsername,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            registerDialog.dispose();
            usernameField.setText(newUsername);
            passwordField.setText("");
        });

        cancelBtn.addActionListener(e -> registerDialog.dispose());

        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(buttonPanel, gbc);

        registerDialog.add(panel);
        registerDialog.setVisible(true);
    }

    // ===== MAIN APPLICATION =====

    private void initializeMainApplication() {
        setTitle("Stock Trading Platform");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        createMenuBar();
        createMainPanel();
        createStatusBar();

        startMarketSimulation();

        refreshAllTables();
        updateDashboardStats();

        setVisible(true);
    }

    private void initializeDefaultUsers() {
        User admin = new User("U001", "admin", "admin123", "System Administrator", "admin@stocks.com");
        admin.setUserType("Admin");
        users.put("admin", admin);
        db.saveUser(admin);

        User trader = new User("U002", "trader", "trader123", "John Trader", "john@email.com");
        users.put("trader", trader);
        db.saveUser(trader);

        User demo = new User("U003", "demo", "demo123", "Demo User", "demo@email.com");
        users.put("demo", demo);
        db.saveUser(demo);

        for (User user : users.values()) {
            Portfolio portfolio = new Portfolio(user.getUserId());
            portfolios.put(user.getUserId(), portfolio);
            db.savePortfolio(portfolio);
        }

        System.out.println("[INFO] " + users.size() + " users initialized.");
    }

    private void loadAllData() {
        users = db.loadAllUsers();
        portfolios = db.loadAllPortfolios();
        transactions = db.loadAllTransactions();

        for (Portfolio p : portfolios.values()) {
            p.updateCurrentValue(stockMarket.getAllStocks());
        }
    }

    private void saveAllData() {
        for (User user : users.values()) {
            db.saveUser(user);
        }
        for (Portfolio portfolio : portfolios.values()) {
            db.savePortfolio(portfolio);
        }
        for (Transaction transaction : transactions) {
            db.saveTransaction(transaction);
        }
    }

    private void startMarketSimulation() {
        marketTimer = new Timer();
        marketTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                stockMarket.simulateMarketMovement();
                refreshAllTables();
                updateDashboardStats();
                updateMarketStatus();
            }
        }, 0, 5000);
    }

    private void updateMarketStatus() {
        if (marketStatusLabel != null) {
            StockMarket.MarketStats stats = stockMarket.getMarketStats();
            marketStatusLabel.setText("Market Day: " + stats.marketDay + " | Up: " + stats.stocksUp + " | Down: " + stats.stocksDown);
        }
    }

    private void showLoginDialog() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (marketTimer != null) {
                marketTimer.cancel();
            }
            saveAllData();
            dispose();
            currentUser = null;
            showLoginScreen();
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?\nAll data will be saved automatically.",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (marketTimer != null) {
                marketTimer.cancel();
            }
            saveAllData();
            System.exit(0);
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> showLoginDialog());

        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.addActionListener(e -> {
            refreshAllTables();
            updateDashboardStats();
            updateMarketStatus();
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> confirmExit());

        fileMenu.add(logoutItem);
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu marketMenu = new JMenu("Market");
        JMenuItem openMarketItem = new JMenuItem("Open Market");
        openMarketItem.addActionListener(e -> {
            stockMarket.openMarket();
            JOptionPane.showMessageDialog(this, "Market opened!");
        });

        JMenuItem closeMarketItem = new JMenuItem("Close Market");
        closeMarketItem.addActionListener(e -> {
            stockMarket.closeMarket();
            JOptionPane.showMessageDialog(this, "Market closed!");
        });

        JMenuItem nextDayItem = new JMenuItem("Next Market Day");
        nextDayItem.addActionListener(e -> {
            stockMarket.nextMarketDay();
            updateMarketStatus();
            JOptionPane.showMessageDialog(this, "Market day: " + stockMarket.getMarketDay());
        });

        marketMenu.add(openMarketItem);
        marketMenu.add(closeMarketItem);
        marketMenu.addSeparator();
        marketMenu.add(nextDayItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Stock Trading Platform\nVersion 2.0\n\n" +
                            "Simulated stock trading environment\n" +
                            "Features:\n" +
                            " Real-time market data\n" +
                            " Buy and sell stocks\n" +
                            " Portfolio tracking\n" +
                            " Transaction history\n" +
                            " SQLite Database Storage\n" +
                            " Multi-user support\n\n" +
                            "Developed for CodeAlpha Internship",
                    "About", JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(marketMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBackground(new Color(240, 240, 240));

        userLabel = new JLabel("User: " + (currentUser != null ? currentUser.getUsername() : "Not Logged In"));
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        balanceLabel = new JLabel("Cash: R0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        portfolioValueLabel = new JLabel("Portfolio: R0.00");
        portfolioValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        profitLossLabel = new JLabel("P/L: R0.00 (0.00%)");
        profitLossLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        marketStatusLabel = new JLabel("Market Day: 1 | Up: 0 | Down: 0");
        marketStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statusBar.add(userLabel);
        statusBar.add(Box.createHorizontalStrut(15));
        statusBar.add(balanceLabel);
        statusBar.add(Box.createHorizontalStrut(15));
        statusBar.add(portfolioValueLabel);
        statusBar.add(Box.createHorizontalStrut(15));
        statusBar.add(profitLossLabel);
        statusBar.add(Box.createHorizontalStrut(15));
        statusBar.add(marketStatusLabel);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void createMainPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Market", createMarketPanel());
        tabbedPane.addTab("Portfolio", createPortfolioPanel());
        tabbedPane.addTab("Trade", createTradePanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 244, 248));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 244, 248));

        JLabel headerLabel = new JLabel("Stock Trading Platform Dashboard");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(headerLabel, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setBackground(new Color(240, 244, 248));
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        StockMarket.MarketStats stats = stockMarket.getMarketStats();
        statsPanel.add(createStatCard("Total Stocks", String.valueOf(stats.totalStocks), new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Active Users", String.valueOf(users.size()), new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Transactions", String.valueOf(transactions.size()), new Color(155, 89, 182)));
        statsPanel.add(createStatCard("Market Day", String.valueOf(stats.marketDay), new Color(241, 196, 15)));

        panel.add(statsPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBackground(new Color(240, 244, 248));

        JButton refreshBtn = createLoginButton("Refresh Data", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            refreshAllTables();
            updateDashboardStats();
            updateMarketStatus();
        });

        JButton tradeBtn = createLoginButton("Trade Stocks", new Color(46, 204, 113));
        tradeBtn.addActionListener(e -> tabbedPane.setSelectedIndex(3));

        JButton logoutBtn = createLoginButton("Logout", new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> showLoginDialog());

        actionPanel.add(refreshBtn);
        actionPanel.add(tradeBtn);
        actionPanel.add(logoutBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = createLoginButton("Refresh Market", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Market data refreshed!");
        });

        JButton topBtn = createLoginButton("Top Performers", new Color(46, 204, 113));
        topBtn.addActionListener(e -> showTopPerformers());

        JButton worstBtn = createLoginButton("Worst Performers", new Color(231, 76, 60));
        worstBtn.addActionListener(e -> showWorstPerformers());

        toolbar.add(refreshBtn);
        toolbar.add(topBtn);
        toolbar.add(worstBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"Symbol", "Name", "Price (R)", "Change", "High", "Low", "Volume", "Sector"};
        marketTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        marketTable = new JTable(marketTableModel);
        marketTable.setRowHeight(25);
        marketTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        marketTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(marketTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void showTopPerformers() {
        List<Stock> top = stockMarket.getTopPerformers(5);
        StringBuilder msg = new StringBuilder("Top 5 Performers:\n\n");
        for (Stock s : top) {
            msg.append(s).append("\n");
        }
        JOptionPane.showMessageDialog(this, msg.toString(), "Top Performers", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWorstPerformers() {
        List<Stock> worst = stockMarket.getWorstPerformers(5);
        StringBuilder msg = new StringBuilder("Worst 5 Performers:\n\n");
        for (Stock s : worst) {
            msg.append(s).append("\n");
        }
        JOptionPane.showMessageDialog(this, msg.toString(), "Worst Performers", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = createLoginButton("Refresh Portfolio", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            refreshAllTables();
            updateDashboardStats();
        });

        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"Symbol", "Shares", "Avg Price", "Current Price", "Value", "P/L"};
        portfolioTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        portfolioTable = new JTable(portfolioTableModel);
        portfolioTable.setRowHeight(25);
        portfolioTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        portfolioTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTradePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel tradeForm = new JPanel(new GridBagLayout());
        tradeForm.setBorder(BorderFactory.createTitledBorder("Place Trade"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        stockCombo = new JComboBox<>();
        for (String symbol : stockMarket.getAllStocks().keySet()) {
            stockCombo.addItem(symbol + " - " + stockMarket.getStock(symbol).getName());
        }

        sharesField = new JTextField();

        buyButton = createLoginButton("BUY", new Color(46, 204, 113));
        sellButton = createLoginButton("SELL", new Color(231, 76, 60));

        JLabel currentPriceLabel = new JLabel("Current Price: ");
        JLabel priceValueLabel = new JLabel("R0.00");
        priceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        stockCombo.addActionListener(e -> {
            String selected = (String) stockCombo.getSelectedItem();
            if (selected != null) {
                String symbol = selected.split(" - ")[0];
                Stock stock = stockMarket.getStock(symbol);
                if (stock != null) {
                    priceValueLabel.setText("R" + df.format(stock.getCurrentPrice()));
                }
            }
        });

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        tradeForm.add(new JLabel("Select Stock:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        tradeForm.add(stockCombo, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        tradeForm.add(new JLabel("Current Price:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        tradeForm.add(priceValueLabel, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        tradeForm.add(new JLabel("Number of Shares:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        tradeForm.add(sharesField, gbc);

        row++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 3;
        tradeForm.add(buttonPanel, gbc);

        buyButton.addActionListener(e -> executeTrade("BUY"));
        sellButton.addActionListener(e -> executeTrade("SELL"));

        panel.add(tradeForm, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Account Info"));
        infoPanel.setBackground(Color.WHITE);

        JLabel cashLabel = new JLabel("Cash Balance: ");
        JLabel cashValue = new JLabel("R0.00");
        cashValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cashValue.setForeground(new Color(46, 204, 113));

        JLabel holdingsLabel = new JLabel("Holdings: ");
        JLabel holdingsValue = new JLabel("0");
        holdingsValue.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel plLabel = new JLabel("Total P/L: ");
        JLabel plValue = new JLabel("R0.00");
        plValue.setFont(new Font("Segoe UI", Font.BOLD, 14));

        Timer infoTimer = new Timer();
        infoTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentUser != null) {
                    cashValue.setText("R" + df.format(currentUser.getCashBalance()));
                    Portfolio p = portfolios.get(currentUser.getUserId());
                    if (p != null) {
                        p.updateCurrentValue(stockMarket.getAllStocks());
                        int totalShares = 0;
                        for (int shares : p.getHoldings().values()) {
                            totalShares += shares;
                        }
                        holdingsValue.setText(String.valueOf(totalShares));
                        double pl = p.getProfitLoss();
                        plValue.setText("R" + df.format(pl));
                        plValue.setForeground(pl >= 0 ? new Color(46, 204, 113) : new Color(231, 76, 60));
                    }
                }
            }
        }, 0, 1000);

        JPanel cashPanel = new JPanel(new FlowLayout());
        cashPanel.add(cashLabel);
        cashPanel.add(cashValue);

        JPanel holdingsPanel = new JPanel(new FlowLayout());
        holdingsPanel.add(holdingsLabel);
        holdingsPanel.add(holdingsValue);

        JPanel plPanel = new JPanel(new FlowLayout());
        plPanel.add(plLabel);
        plPanel.add(plValue);

        infoPanel.add(cashPanel);
        infoPanel.add(holdingsPanel);
        infoPanel.add(plPanel);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = createLoginButton("Refresh Transactions", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> refreshAllTables());

        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"Transaction ID", "Type", "Stock", "Shares", "Price", "Total", "Status", "Time"};
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setRowHeight(25);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void executeTrade(String type) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please login first!");
            return;
        }

        String selected = (String) stockCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a stock!");
            return;
        }

        String symbol = selected.split(" - ")[0];
        Stock stock = stockMarket.getStock(symbol);
        if (stock == null) {
            JOptionPane.showMessageDialog(this, "Invalid stock selected!");
            return;
        }

        int shares;
        try {
            shares = Integer.parseInt(sharesField.getText().trim());
            if (shares <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive number of shares!");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of shares!");
            return;
        }

        double totalCost = shares * stock.getCurrentPrice();
        Portfolio portfolio = portfolios.get(currentUser.getUserId());

        if (type.equals("BUY")) {
            if (currentUser.getCashBalance() < totalCost) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient funds!\nRequired: R" + df.format(totalCost) +
                                "\nAvailable: R" + df.format(currentUser.getCashBalance()));
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Confirm BUY:\n" +
                            "Stock: " + symbol + "\n" +
                            "Shares: " + shares + "\n" +
                            "Price: R" + df.format(stock.getCurrentPrice()) + "\n" +
                            "Total: R" + df.format(totalCost),
                    "Confirm Trade", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                currentUser.withdraw(totalCost);
                portfolio.addHolding(symbol, shares, stock.getCurrentPrice());

                transactionCounter++;
                String transactionId = "T" + transactionCounter;
                Transaction transaction = new Transaction(
                        transactionId, currentUser.getUserId(),
                        symbol, type, shares, stock.getCurrentPrice()
                );
                transactions.add(transaction);

                db.updateUserCash(currentUser);
                db.savePortfolio(portfolio);
                db.saveTransaction(transaction);

                refreshAllTables();
                updateDashboardStats();

                JOptionPane.showMessageDialog(this,
                        "BUY executed successfully!\n" +
                                shares + " shares of " + symbol + " purchased.");
            }
        } else {
            if (!portfolio.hasStock(symbol)) {
                JOptionPane.showMessageDialog(this, "You don't own any shares of " + symbol + "!");
                return;
            }

            int ownedShares = portfolio.getShares(symbol);
            if (ownedShares < shares) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient shares!\nOwned: " + ownedShares + "\nRequested: " + shares);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Confirm SELL:\n" +
                            "Stock: " + symbol + "\n" +
                            "Shares: " + shares + "\n" +
                            "Price: R" + df.format(stock.getCurrentPrice()) + "\n" +
                            "Total: R" + df.format(totalCost),
                    "Confirm Trade", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                currentUser.deposit(totalCost);
                portfolio.removeHolding(symbol, shares, stock.getCurrentPrice());

                transactionCounter++;
                String transactionId = "T" + transactionCounter;
                Transaction transaction = new Transaction(
                        transactionId, currentUser.getUserId(),
                        symbol, type, shares, stock.getCurrentPrice()
                );
                transactions.add(transaction);

                db.updateUserCash(currentUser);
                db.savePortfolio(portfolio);
                db.saveTransaction(transaction);

                refreshAllTables();
                updateDashboardStats();

                JOptionPane.showMessageDialog(this,
                        "SELL executed successfully!\n" +
                                shares + " shares of " + symbol + " sold.");
            }
        }
    }

    private void refreshAllTables() {
        refreshMarketTable();
        refreshPortfolioTable();
        refreshTransactionTable();
    }

    private void refreshMarketTable() {
        marketTableModel.setRowCount(0);
        for (Stock stock : stockMarket.getAllStocks().values()) {
            marketTableModel.addRow(new Object[]{
                    stock.getSymbol(),
                    stock.getName(),
                    "R" + df.format(stock.getCurrentPrice()),
                    stock.getPriceChangeDisplay(),
                    "R" + df.format(stock.getHighPrice()),
                    "R" + df.format(stock.getLowPrice()),
                    stock.getVolume(),
                    stock.getSector()
            });
        }
    }

    private void refreshPortfolioTable() {
        portfolioTableModel.setRowCount(0);
        if (currentUser == null) return;

        Portfolio portfolio = portfolios.get(currentUser.getUserId());
        if (portfolio == null) return;

        portfolio.updateCurrentValue(stockMarket.getAllStocks());

        for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            Stock stock = stockMarket.getStock(symbol);
            if (stock != null) {
                double avgPrice = portfolio.getTotalInvested() /
                        (portfolio.getHoldings().values().stream().mapToInt(Integer::intValue).sum() > 0 ?
                                portfolio.getHoldings().values().stream().mapToInt(Integer::intValue).sum() : 1);
                double value = shares * stock.getCurrentPrice();
                double pl = value - (shares * avgPrice);

                portfolioTableModel.addRow(new Object[]{
                        symbol,
                        shares,
                        "R" + df.format(avgPrice),
                        "R" + df.format(stock.getCurrentPrice()),
                        "R" + df.format(value),
                        "R" + df.format(pl)
                });
            }
        }
    }

    private void refreshTransactionTable() {
        transactionTableModel.setRowCount(0);
        if (currentUser == null) return;

        for (Transaction t : transactions) {
            if (t.getUserId().equals(currentUser.getUserId())) {
                transactionTableModel.addRow(new Object[]{
                        t.getTransactionId(),
                        t.getType(),
                        t.getStockSymbol(),
                        t.getShares(),
                        "R" + df.format(t.getPrice()),
                        "R" + df.format(t.getTotalAmount()),
                        t.getStatus(),
                        t.getFormattedTimestamp()
                });
            }
        }
    }

    private void updateDashboardStats() {
        if (currentUser != null) {
            balanceLabel.setText("Cash: R" + df.format(currentUser.getCashBalance()));

            Portfolio portfolio = portfolios.get(currentUser.getUserId());
            if (portfolio != null) {
                portfolio.updateCurrentValue(stockMarket.getAllStocks());
                portfolioValueLabel.setText("Portfolio: R" + df.format(portfolio.getCurrentValue()));
                double pl = portfolio.getProfitLoss();
                profitLossLabel.setText("P/L: R" + df.format(pl) + " (" + df.format(portfolio.getProfitLossPercent()) + "%)");
                profitLossLabel.setForeground(pl >= 0 ? new Color(46, 204, 113) : new Color(231, 76, 60));
            }
        }
    }
}