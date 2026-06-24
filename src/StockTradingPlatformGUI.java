import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class StockTradingPlatformGUI extends JFrame {

    private Map<String, Stock> stocks;
    private Map<String, User> users;
    private Map<String, Portfolio> portfolios;
    private List<Transaction> transactions;

    private User currentUser;
    private Timer marketTimer;

    private static final String DATA_DIR = "stock_data";
    private static final String STOCKS_FILE = DATA_DIR + "/stocks.dat";
    private static final String USERS_FILE = DATA_DIR + "/users.dat";
    private static final String PORTFOLIOS_FILE = DATA_DIR + "/portfolios.dat";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.dat";

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

    private JComboBox<String> stockCombo;
    private JTextField sharesField;
    private JButton buyButton;
    private JButton sellButton;

    private DecimalFormat df = new DecimalFormat("#0.00");

    public StockTradingPlatformGUI() {
        stocks = new HashMap<>();
        users = new HashMap<>();
        portfolios = new HashMap<>();
        transactions = new ArrayList<>();

        createDataDirectory();
        loadAllData();

        if (stocks.isEmpty()) {
            initializeDefaultStocks();
        }
        if (users.isEmpty()) {
            initializeDefaultUsers();
        }

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

        loginUser("admin", "admin123");

        startMarketSimulation();

        refreshAllTables();
        updateDashboardStats();

        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new StockTradingPlatformGUI());
    }

    private void createDataDirectory() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private void initializeDefaultStocks() {
        String[][] stockData = {
                {"AAPL", "Apple Inc.", "185.50", "Technology"},
                {"GOOGL", "Alphabet Inc.", "140.75", "Technology"},
                {"MSFT", "Microsoft Corp.", "375.20", "Technology"},
                {"AMZN", "Amazon.com Inc.", "145.30", "Consumer"},
                {"TSLA", "Tesla Inc.", "245.80", "Automotive"},
                {"NFLX", "Netflix Inc.", "480.50", "Entertainment"},
                {"JPM", "JPMorgan Chase", "155.60", "Financial"},
                {"VTI", "Vanguard Total Stock", "260.40", "ETF"},
                {"NKE", "Nike Inc.", "105.20", "Consumer"},
                {"NVDA", "NVIDIA Corp.", "495.30", "Technology"}
        };

        for (String[] data : stockData) {
            Stock stock = new Stock(data[0], data[1], Double.parseDouble(data[2]), data[3]);
            stocks.put(data[0], stock);
        }

        saveStocks();
        System.out.println("[INFO] " + stocks.size() + " stocks initialized.");
    }

    private void initializeDefaultUsers() {
        User admin = new User("U001", "admin", "admin123", "System Administrator", "admin@stocks.com");
        admin.setUserType("Admin");
        users.put("admin", admin);

        User trader = new User("U002", "trader", "trader123", "John Trader", "john@email.com");
        users.put("trader", trader);

        for (User user : users.values()) {
            portfolios.put(user.getUserId(), new Portfolio(user.getUserId()));
        }

        saveUsers();
        savePortfolios();
        System.out.println("[INFO] " + users.size() + " users initialized.");
    }

    @SuppressWarnings("unchecked")
    private void loadAllData() {
        loadStocks();
        loadUsers();
        loadPortfolios();
        loadTransactions();
    }

    private void saveAllData() {
        saveStocks();
        saveUsers();
        savePortfolios();
        saveTransactions();
    }

    private void saveStocks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STOCKS_FILE))) {
            oos.writeObject(stocks);
        } catch (IOException e) {
            // Silent fail
        }
    }

    @SuppressWarnings("unchecked")
    private void loadStocks() {
        File file = new File(STOCKS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            stocks = (Map<String, Stock>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Silent fail
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            // Silent fail
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Silent fail
        }
    }

    private void savePortfolios() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PORTFOLIOS_FILE))) {
            oos.writeObject(portfolios);
        } catch (IOException e) {
            // Silent fail
        }
    }

    @SuppressWarnings("unchecked")
    private void loadPortfolios() {
        File file = new File(PORTFOLIOS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            portfolios = (Map<String, Portfolio>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Silent fail
        }
    }

    private void saveTransactions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRANSACTIONS_FILE))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            // Silent fail
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTransactions() {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            transactions = (List<Transaction>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Silent fail
        }
    }

    private void startMarketSimulation() {
        marketTimer = new Timer();
        marketTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                simulateMarketMovement();
                refreshAllTables();
                updateDashboardStats();
            }
        }, 0, 5000);
    }

    private void simulateMarketMovement() {
        Random rand = new Random();
        for (Stock stock : stocks.values()) {
            double change = (rand.nextDouble() * 4) - 2;
            double newPrice = stock.getCurrentPrice() * (1 + change / 100);
            newPrice = Math.round(newPrice * 100.0) / 100.0;
            if (newPrice > 0) {
                stock.setCurrentPrice(newPrice);
                stock.setVolume(1000 + rand.nextInt(9000));
            }
        }
    }

    private void loginUser(String username, String password) {
        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
            currentUser = users.get(username);
            userLabel.setText("User: " + username);
            updateDashboardStats();
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Welcome " + currentUser.getFullName() + "!");
        }
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            loginUser(username, password);
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
        logoutItem.addActionListener(e -> {
            currentUser = null;
            userLabel.setText("User: Not Logged In");
            JOptionPane.showMessageDialog(this, "Logged out successfully!");
            showLoginDialog();
        });

        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.addActionListener(e -> {
            refreshAllTables();
            updateDashboardStats();
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> confirmExit());

        fileMenu.add(logoutItem);
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

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
                            " Transaction history\n\n" +
                            "Developed for CodeAlpha Internship",
                    "About", JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBackground(new Color(240, 240, 240));

        userLabel = new JLabel("User: Not Logged In");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        balanceLabel = new JLabel("Cash: R0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        portfolioValueLabel = new JLabel("Portfolio: R0.00");
        portfolioValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        profitLossLabel = new JLabel("P/L: R0.00 (0.00%)");
        profitLossLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statusBar.add(userLabel);
        statusBar.add(Box.createHorizontalStrut(20));
        statusBar.add(balanceLabel);
        statusBar.add(Box.createHorizontalStrut(20));
        statusBar.add(portfolioValueLabel);
        statusBar.add(Box.createHorizontalStrut(20));
        statusBar.add(profitLossLabel);

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

        statsPanel.add(createStatCard("Total Stocks", String.valueOf(stocks.size()), new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Active Users", String.valueOf(users.size()), new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Transactions", String.valueOf(transactions.size()), new Color(155, 89, 182)));
        statsPanel.add(createStatCard("Market Cap", "R" + getTotalMarketCap(), new Color(241, 196, 15)));

        panel.add(statsPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBackground(new Color(240, 244, 248));

        JButton refreshBtn = createStyledButton("Refresh Data", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            refreshAllTables();
            updateDashboardStats();
        });

        JButton tradeBtn = createStyledButton("Trade Stocks", new Color(46, 204, 113));
        tradeBtn.addActionListener(e -> tabbedPane.setSelectedIndex(3));

        JButton logoutBtn = createStyledButton("Logout", new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            userLabel.setText("User: Not Logged In");
            showLoginDialog();
        });

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

        JButton refreshBtn = createStyledButton("Refresh Market", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Market data refreshed!");
        });

        toolbar.add(refreshBtn);
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

    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = createStyledButton("Refresh Portfolio", new Color(52, 152, 219));
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
        for (String symbol : stocks.keySet()) {
            stockCombo.addItem(symbol + " - " + stocks.get(symbol).getName());
        }

        sharesField = new JTextField();

        buyButton = createStyledButton("BUY", new Color(46, 204, 113));
        sellButton = createStyledButton("SELL", new Color(231, 76, 60));

        JLabel currentPriceLabel = new JLabel("Current Price: ");
        JLabel priceValueLabel = new JLabel("R0.00");
        priceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        stockCombo.addActionListener(e -> {
            String selected = (String) stockCombo.getSelectedItem();
            if (selected != null) {
                String symbol = selected.split(" - ")[0];
                Stock stock = stocks.get(symbol);
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
                        p.updateCurrentValue(stocks);
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

        JButton refreshBtn = createStyledButton("Refresh Transactions", new Color(52, 152, 219));
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
        Stock stock = stocks.get(symbol);
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
                Transaction transaction = new Transaction(
                        "T" + transactionCounter, currentUser.getUserId(),
                        symbol, type, shares, stock.getCurrentPrice()
                );
                transactions.add(transaction);

                saveAllData();
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
                Transaction transaction = new Transaction(
                        "T" + transactionCounter, currentUser.getUserId(),
                        symbol, type, shares, stock.getCurrentPrice()
                );
                transactions.add(transaction);

                saveAllData();
                refreshAllTables();
                updateDashboardStats();

                JOptionPane.showMessageDialog(this,
                        "SELL executed successfully!\n" +
                                shares + " shares of " + symbol + " sold.");
            }
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String getTotalMarketCap() {
        double total = 0;
        for (Stock stock : stocks.values()) {
            total += stock.getMarketCap();
        }
        return df.format(total / 1000000) + "M";
    }

    private void refreshAllTables() {
        refreshMarketTable();
        refreshPortfolioTable();
        refreshTransactionTable();
    }

    private void refreshMarketTable() {
        marketTableModel.setRowCount(0);
        for (Stock stock : stocks.values()) {
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

        portfolio.updateCurrentValue(stocks);

        for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            Stock stock = stocks.get(symbol);
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
                portfolio.updateCurrentValue(stocks);
                portfolioValueLabel.setText("Portfolio: R" + df.format(portfolio.getCurrentValue()));
                double pl = portfolio.getProfitLoss();
                profitLossLabel.setText("P/L: R" + df.format(pl) + " (" + df.format(portfolio.getProfitLossPercent()) + "%)");
                profitLossLabel.setForeground(pl >= 0 ? new Color(46, 204, 113) : new Color(231, 76, 60));
            }
        }
    }
}