import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private Map<String, Integer> holdings;
    private double totalInvested;
    private double currentValue;

    public Portfolio(String userId) {
        this.userId = userId;
        this.holdings = new HashMap<>();
        this.totalInvested = 0;
        this.currentValue = 0;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Integer> getHoldings() {
        return holdings;
    }

    public double getTotalInvested() {
        return totalInvested;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void addHolding(String symbol, int shares, double price) {
        holdings.put(symbol, holdings.getOrDefault(symbol, 0) + shares);
        totalInvested += shares * price;
        updateCurrentValue(null);
    }

    public boolean removeHolding(String symbol, int shares, double price) {
        if (!holdings.containsKey(symbol) || holdings.get(symbol) < shares) {
            return false;
        }

        int remaining = holdings.get(symbol) - shares;
        if (remaining == 0) {
            holdings.remove(symbol);
        } else {
            holdings.put(symbol, remaining);
        }
        totalInvested -= shares * price;
        updateCurrentValue(null);
        return true;
    }

    public int getShares(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    public void updateCurrentValue(Map<String, Stock> stockMap) {
        currentValue = 0;
        if (stockMap == null) return;

        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            Stock stock = stockMap.get(symbol);
            if (stock != null) {
                currentValue += shares * stock.getCurrentPrice();
            }
        }
    }

    public double getProfitLoss() {
        return currentValue - totalInvested;
    }

    public double getProfitLossPercent() {
        if (totalInvested == 0) return 0;
        return ((currentValue - totalInvested) / totalInvested) * 100;
    }

    public boolean hasStock(String symbol) {
        return holdings.containsKey(symbol) && holdings.get(symbol) > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio for User: ").append(userId).append("\n");
        sb.append("Holdings:\n");
        if (holdings.isEmpty()) {
            sb.append("  No holdings\n");
        } else {
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" shares\n");
            }
        }
        sb.append("Total Invested: R").append(String.format("%.2f", totalInvested)).append("\n");
        sb.append("Current Value: R").append(String.format("%.2f", currentValue)).append("\n");
        sb.append("Profit/Loss: R").append(String.format("%.2f", getProfitLoss()));
        return sb.toString();
    }
}