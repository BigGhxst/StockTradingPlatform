import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private double cashBalance;
    private String userType;

    public User(String userId, String username, String password, String fullName, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.cashBalance = 100000.00;
        this.userType = "Trader";
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public String getUserType() {
        return userType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.cashBalance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.cashBalance >= amount) {
            this.cashBalance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("User: %s | %s | Balance: R%.2f | Type: %s",
                username, fullName, cashBalance, userType);
    }
}