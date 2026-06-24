import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String userId;
    private String stockSymbol;
    private String type;
    private int shares;
    private double price;
    private double totalAmount;
    private LocalDateTime timestamp;
    private String status;

    public Transaction(String transactionId, String userId, String stockSymbol,
                       String type, int shares, double price) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.stockSymbol = stockSymbol;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.totalAmount = shares * price;
        this.timestamp = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getType() {
        return type;
    }

    public int getShares() {
        return shares;
    }

    public double getPrice() {
        return price;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %d shares @ R%.2f | Total: R%.2f | %s",
                transactionId, type, stockSymbol, shares, price, totalAmount, getFormattedTimestamp());
    }
}