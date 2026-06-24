import java.io.Serializable;
import java.text.DecimalFormat;

public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String name;
    private double currentPrice;
    private double previousPrice;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private int volume;
    private String sector;
    private double marketCap;

    public Stock(String symbol, String name, double currentPrice, String sector) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.previousPrice = currentPrice;
        this.openPrice = currentPrice;
        this.highPrice = currentPrice;
        this.lowPrice = currentPrice;
        this.sector = sector;
        this.volume = 0;
        this.marketCap = currentPrice * 1000000;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public int getVolume() {
        return volume;
    }

    public String getSector() {
        return sector;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public void setCurrentPrice(double currentPrice) {
        this.previousPrice = this.currentPrice;
        this.currentPrice = currentPrice;
        if (currentPrice > highPrice) {
            this.highPrice = currentPrice;
        }
        if (currentPrice < lowPrice || lowPrice == 0) {
            this.lowPrice = currentPrice;
        }
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }

    public double getPriceChange() {
        return currentPrice - previousPrice;
    }

    public double getPriceChangePercent() {
        if (previousPrice == 0) return 0;
        return ((currentPrice - previousPrice) / previousPrice) * 100;
    }

    public String getPriceChangeDisplay() {
        DecimalFormat df = new DecimalFormat("#0.00");
        double change = getPriceChange();
        double percent = getPriceChangePercent();
        String sign = change >= 0 ? "+" : "";
        return sign + df.format(change) + " (" + sign + df.format(percent) + "%)";
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#0.00");
        return String.format("%-6s %-20s R%s %s",
                symbol, name, df.format(currentPrice), getPriceChangeDisplay());
    }
}