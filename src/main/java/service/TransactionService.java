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

    public boolean deleteTransaction(int index){
        if (index<0 || index >= transactions.size()){
            return false;
        }
        transactions.remove(index);
        return true;
    }

    public boolean editTransaction(int index, Transaction update){
        if (index<0 || index >= transactions.size()){
            return false;
        }
        transactions.set(index, update);
        return true;
    }
}
