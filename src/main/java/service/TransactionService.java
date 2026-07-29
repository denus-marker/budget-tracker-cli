package service;

import model.Transaction;
import model.TransactionType;

import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t){
        transactions.add(t);
    }

    public List<Transaction> getAllTransactions(){
        return transactions;
    }

    public double getBalance(){
        double balance = 0;
        for (Transaction t : transactions){
            if (t.getType() == TransactionType.INCOME){
                balance += t.getAmount();
            } else {
                balance -= t.getAmount();
            }
        }
        return balance;
    }

    public List<Transaction> getByCategory(String category){
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions){
            if (t.getCategory().equalsIgnoreCase(category)){
                result.add(t);
            }
        }
        return result;
    }
}
