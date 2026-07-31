package model;

import java.time.LocalDate;
import java.util.Objects;

public class Transaction {
    private String description;
    private double amount;
    private String category;
    private TransactionType type;
    private LocalDate date;

    public Transaction(String description, double amount, String category, TransactionType type, LocalDate date) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString(){
        return date + " | "+ type + " | " + category + " | " + amount + " | " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(amount, that.amount) == 0 &&
                Objects.equals(description, that.description) &&
                Objects.equals(category, that.category) &&
                type == that.type &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, amount, category, type, date);
    }

}
