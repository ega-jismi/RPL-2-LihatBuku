package lihatbuku.model;

import jakarta.persistence.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model entitas data transaksi pembayaran langganan membership.
 */
@Entity
@Table(name = "membership_transactions")
public class MembershipTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Transient
    private String userName;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "SUCCESS", "PENDING", "FAILED"

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public MembershipTransaction() {
        this.transactionDate = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public MembershipTransaction(int userId, String planName, int durationDays, double amount,
                                 String paymentMethod, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.planName = planName;
        this.durationDays = durationDays;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = "SUCCESS";
        this.transactionDate = LocalDateTime.now();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() {
        if (!"SUCCESS".equalsIgnoreCase(status) || endDate == null) return false;
        LocalDate today = LocalDate.now();
        return !today.isAfter(endDate);
    }

    public String getFormattedAmount() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        return nf.format(amount);
    }

    public String getFormattedTransactionDate() {
        if (transactionDate == null) return "-";
        return transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getFormattedPeriod() {
        if (startDate == null || endDate == null) return "-";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return startDate.format(dtf) + " s/d " + endDate.format(dtf);
    }
}
