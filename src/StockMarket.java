import java.text.DecimalFormat;
import java.util.*;

public class StockMarket {

    private Map<String, Stock> stocks;
    private transient Random random;
    private transient DecimalFormat df;
    private StockDatabase db;

    private boolean marketOpen = true;
    private int marketDay = 1;

    public StockMarket() {
        this.stocks = new HashMap<>();
        this.random = new Random();
        this.df = new DecimalFormat("#0.00");

        // Initialize database - this will load the driver
        try {
            this.db = new StockDatabase();
            System.out.println("[INFO] StockMarket connected to database.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize database: " + e.getMessage());
            this.db = null;
        }

        loadStocksFromDatabase();

        if (stocks.isEmpty()) {
            System.out.println("[INFO] No stocks found in database. Initializing default stocks...");
            initializeDefaultStocks();
        }
    }

    private void loadStocksFromDatabase() {
        if (db == null) {
            System.err.println("[ERROR] Database not available. Cannot load stocks.");
            return;
        }

        try {
            stocks = db.loadAllStocks();
            System.out.println("[INFO] Loaded " + stocks.size() + " stocks from database.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load stocks: " + e.getMessage());
            stocks = new HashMap<>();
        }
    }

    private void initializeDefaultStocks() {
        if (db == null) {
            System.err.println("[ERROR] Database not available. Cannot initialize stocks.");
            return;
        }

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
            try {
                Stock stock = new Stock(data[0], data[1], Double.parseDouble(data[2]), data[3]);
                stocks.put(data[0], stock);
                db.saveStock(stock);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to save stock " + data[0] + ": " + e.getMessage());
            }
        }

        System.out.println("[INFO] " + stocks.size() + " default stocks initialized and saved to database.");
    }

    public void saveStockToDatabase(Stock stock) {
        if (db == null) {
            System.err.println("[ERROR] Database not available. Cannot save stock.");
            return;
        }

        try {
            stocks.put(stock.getSymbol(), stock);
            db.saveStock(stock);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to save stock to database: " + e.getMessage());
        }
    }

    public void simulateMarketMovement() {
        if (!marketOpen) return;
        if (db == null) return;

        for (Stock stock : stocks.values()) {
            try {
                double changePercent = (random.nextDouble() * 4) - 2;
                double newPrice = stock.getCurrentPrice() * (1 + changePercent / 100);
                newPrice = Math.round(newPrice * 100.0) / 100.0;

                if (newPrice > 0) {
                    stock.setCurrentPrice(newPrice);
                    stock.setVolume(1000 + random.nextInt(9000));
                    db.updateStockPrice(stock);
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to update stock price for " + stock.getSymbol() + ": " + e.getMessage());
            }
        }
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public Map<String, Stock> getAllStocks() {
        return stocks;
    }

    public List<Stock> getStocksBySector(String sector) {
        List<Stock> result = new ArrayList<>();
        for (Stock stock : stocks.values()) {
            if (stock.getSector().equalsIgnoreCase(sector)) {
                result.add(stock);
            }
        }
        return result;
    }

    public List<Stock> getTopPerformers(int limit) {
        List<Stock> sorted = new ArrayList<>(stocks.values());
        sorted.sort((s1, s2) -> Double.compare(
                s2.getPriceChangePercent(), s1.getPriceChangePercent()
        ));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public List<Stock> getWorstPerformers(int limit) {
        List<Stock> sorted = new ArrayList<>(stocks.values());
        sorted.sort((s1, s2) -> Double.compare(
                s1.getPriceChangePercent(), s2.getPriceChangePercent()
        ));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public MarketStats getMarketStats() {
        MarketStats stats = new MarketStats();
        stats.totalStocks = stocks.size();

        double totalMarketCap = 0;
        double totalVolume = 0;
        int stocksUp = 0;
        int stocksDown = 0;

        for (Stock stock : stocks.values()) {
            totalMarketCap += stock.getMarketCap();
            totalVolume += stock.getVolume();
            if (stock.getPriceChange() >= 0) {
                stocksUp++;
            } else {
                stocksDown++;
            }
        }

        stats.totalMarketCap = totalMarketCap;
        stats.totalVolume = totalVolume;
        stats.stocksUp = stocksUp;
        stats.stocksDown = stocksDown;
        stats.marketDay = marketDay;

        return stats;
    }

    public void openMarket() {
        this.marketOpen = true;
        System.out.println("[INFO] Market opened for trading.");
    }

    public void closeMarket() {
        this.marketOpen = false;
        System.out.println("[INFO] Market closed for trading.");
    }

    public boolean isMarketOpen() {
        return marketOpen;
    }

    public void nextMarketDay() {
        this.marketDay++;
        System.out.println("[INFO] Market day: " + marketDay);
    }

    public int getMarketDay() {
        return marketDay;
    }

    public static class MarketStats {
        public int totalStocks;
        public double totalMarketCap;
        public double totalVolume;
        public int stocksUp;
        public int stocksDown;
        public int marketDay;

        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat("#0.00");
            return String.format(
                    "Market Day: %d | Stocks: %d | Market Cap: R%sM | Volume: %d | Up: %d | Down: %d",
                    marketDay, totalStocks, df.format(totalMarketCap / 1000000),
                    (long) totalVolume, stocksUp, stocksDown
            );
        }
    }
}