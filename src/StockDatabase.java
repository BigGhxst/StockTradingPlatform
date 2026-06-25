import java.sql.*;
import java.util.*;

public class StockDatabase {

    private static final String DB_URL = "jdbc:sqlite:stock_trading.db";

    public StockDatabase() {
        // Explicitly load the driver
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("[INFO] SQLite JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] SQLite JDBC driver not found: " + e.getMessage());
        }
        createTables();
    }

    private void createTables() {
        String createStocksTable = "CREATE TABLE IF NOT EXISTS stocks (" +
                "symbol TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "current_price REAL, " +
                "previous_price REAL, " +
                "open_price REAL, " +
                "high_price REAL, " +
                "low_price REAL, " +
                "volume INTEGER, " +
                "sector TEXT, " +
                "market_cap REAL)";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id TEXT PRIMARY KEY, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "full_name TEXT, " +
                "email TEXT, " +
                "cash_balance REAL, " +
                "user_type TEXT)";

        String createPortfoliosTable = "CREATE TABLE IF NOT EXISTS portfolios (" +
                "user_id TEXT, " +
                "symbol TEXT, " +
                "shares INTEGER, " +
                "total_invested REAL, " +
                "PRIMARY KEY (user_id, symbol))";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id TEXT PRIMARY KEY, " +
                "user_id TEXT, " +
                "stock_symbol TEXT, " +
                "type TEXT, " +
                "shares INTEGER, " +
                "price REAL, " +
                "total_amount REAL, " +
                "timestamp TEXT, " +
                "status TEXT)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createStocksTable);
            stmt.execute(createUsersTable);
            stmt.execute(createPortfoliosTable);
            stmt.execute(createTransactionsTable);
            System.out.println("[INFO] Database tables created successfully.");
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to create tables: " + e.getMessage());
        }
    }

    // ===== STOCK OPERATIONS =====

    public void saveStock(Stock stock) {
        String sql = "INSERT OR REPLACE INTO stocks (symbol, name, current_price, previous_price, " +
                "open_price, high_price, low_price, volume, sector, market_cap) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, stock.getSymbol());
            pstmt.setString(2, stock.getName());
            pstmt.setDouble(3, stock.getCurrentPrice());
            pstmt.setDouble(4, stock.getPreviousPrice());
            pstmt.setDouble(5, stock.getOpenPrice());
            pstmt.setDouble(6, stock.getHighPrice());
            pstmt.setDouble(7, stock.getLowPrice());
            pstmt.setInt(8, stock.getVolume());
            pstmt.setString(9, stock.getSector());
            pstmt.setDouble(10, stock.getMarketCap());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to save stock: " + e.getMessage());
        }
    }

    public Map<String, Stock> loadAllStocks() {
        Map<String, Stock> stocks = new HashMap<>();
        String sql = "SELECT * FROM stocks";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Stock stock = new Stock(
                        rs.getString("symbol"),
                        rs.getString("name"),
                        rs.getDouble("current_price"),
                        rs.getString("sector")
                );
                stock.setPreviousPrice(rs.getDouble("previous_price"));
                stock.setOpenPrice(rs.getDouble("open_price"));
                stock.setHighPrice(rs.getDouble("high_price"));
                stock.setLowPrice(rs.getDouble("low_price"));
                stock.setVolume(rs.getInt("volume"));
                stock.setMarketCap(rs.getDouble("market_cap"));
                stocks.put(stock.getSymbol(), stock);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load stocks: " + e.getMessage());
        }
        return stocks;
    }

    public void updateStockPrice(Stock stock) {
        String sql = "UPDATE stocks SET current_price = ?, previous_price = ?, " +
                "high_price = ?, low_price = ?, volume = ? WHERE symbol = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, stock.getCurrentPrice());
            pstmt.setDouble(2, stock.getPreviousPrice());
            pstmt.setDouble(3, stock.getHighPrice());
            pstmt.setDouble(4, stock.getLowPrice());
            pstmt.setInt(5, stock.getVolume());
            pstmt.setString(6, stock.getSymbol());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update stock price: " + e.getMessage());
        }
    }

    // ===== USER OPERATIONS =====

    public void saveUser(User user) {
        String sql = "INSERT OR REPLACE INTO users (user_id, username, password, full_name, email, cash_balance, user_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setDouble(6, user.getCashBalance());
            pstmt.setString(7, user.getUserType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to save user: " + e.getMessage());
        }
    }

    public Map<String, User> loadAllUsers() {
        Map<String, User> users = new HashMap<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email")
                );
                user.setCashBalance(rs.getDouble("cash_balance"));
                user.setUserType(rs.getString("user_type"));
                users.put(user.getUsername(), user);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load users: " + e.getMessage());
        }
        return users;
    }

    public void updateUserCash(User user) {
        String sql = "UPDATE users SET cash_balance = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, user.getCashBalance());
            pstmt.setString(2, user.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to update user cash: " + e.getMessage());
        }
    }

    // ===== PORTFOLIO OPERATIONS =====

    public void savePortfolio(Portfolio portfolio) {
        String deleteSql = "DELETE FROM portfolios WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setString(1, portfolio.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to delete portfolio: " + e.getMessage());
        }

        String insertSql = "INSERT INTO portfolios (user_id, symbol, shares, total_invested) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
                pstmt.setString(1, portfolio.getUserId());
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                pstmt.setDouble(4, portfolio.getTotalInvested());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to save portfolio: " + e.getMessage());
        }
    }

    public Map<String, Portfolio> loadAllPortfolios() {
        Map<String, Portfolio> portfolios = new HashMap<>();
        String sql = "SELECT * FROM portfolios";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String symbol = rs.getString("symbol");
                int shares = rs.getInt("shares");

                if (!portfolios.containsKey(userId)) {
                    portfolios.put(userId, new Portfolio(userId));
                }
                Portfolio portfolio = portfolios.get(userId);
                portfolio.getHoldings().put(symbol, shares);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load portfolios: " + e.getMessage());
        }
        return portfolios;
    }

    // ===== TRANSACTION OPERATIONS =====

    public void saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, user_id, stock_symbol, type, shares, price, total_amount, timestamp, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getUserId());
            pstmt.setString(3, transaction.getStockSymbol());
            pstmt.setString(4, transaction.getType());
            pstmt.setInt(5, transaction.getShares());
            pstmt.setDouble(6, transaction.getPrice());
            pstmt.setDouble(7, transaction.getTotalAmount());
            pstmt.setString(8, transaction.getFormattedTimestamp());
            pstmt.setString(9, transaction.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to save transaction: " + e.getMessage());
        }
    }

    public List<Transaction> loadAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getString("transaction_id"),
                        rs.getString("user_id"),
                        rs.getString("stock_symbol"),
                        rs.getString("type"),
                        rs.getInt("shares"),
                        rs.getDouble("price")
                );
                transaction.setStatus(rs.getString("status"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load transactions: " + e.getMessage());
        }
        return transactions;
    }
}