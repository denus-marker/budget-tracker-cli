package service;

import model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TransactionServiceTest {

    @Test
    void balanceShouldBeZeroWhenNoTransaction(){
        TransactionService service = new TransactionService();
        double balance = service.getBalance();
        assertEquals(0,balance);
    }

    @Test
    void balanceShouldIncreaseWithIncome() {
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction("test1", 200, "test1", TransactionType.INCOME, LocalDate.now()));
        assertEquals(200, service.getBalance());
    }

    @Test
    void balanceShouldDecreaseWithExpense(){
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction("test22", 100, "test22", TransactionType.EXPENSE, LocalDate.now()));
        assertEquals(-100, service.getBalance());
    }

    @Test
    void deleteTransactionShouldRemoveItByIndex(){
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction("test33", 100, "test33", TransactionType.EXPENSE, LocalDate.now()));

        boolean ok = service.deleteTransaction(0);
        assertEquals(true, ok);
        assertEquals(Collections.emptyList(), service.getAllTransactions());
    }

    @Test
    void deleteTransactionShouldReturnFalseForInvalidIndex() {
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction("test44", 100, "test44", TransactionType.EXPENSE, LocalDate.now()));

        boolean ok = service.deleteTransaction(12);
        assertFalse(ok);
    }

    @Test
    void getByCategoryShouldReturnCorrectDescriptionsInOrder(){
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction("test50", 101, "test50", TransactionType.EXPENSE, LocalDate.now()));
        service.addTransaction(new Transaction("test51", 100, "test51", TransactionType.EXPENSE, LocalDate.now()));
        service.addTransaction(new Transaction("test52", 102, "test50", TransactionType.EXPENSE, LocalDate.now()));


        List<Transaction> res = service.getByCategory("test50");

        List<Transaction> expected = new ArrayList<>();
        expected.add(new Transaction("test50", 101, "test50", TransactionType.EXPENSE, LocalDate.now()));
        expected.add(new Transaction("test52", 102, "test50", TransactionType.EXPENSE, LocalDate.now()));

        assertEquals(expected, res);
    }

    @Test
    void getByCategoryShouldReturnOnlyMatchingTransactions2(){
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction("test50", 101, "test50", TransactionType.EXPENSE, LocalDate.now()));
        service.addTransaction(new Transaction("test51", 100, "test51", TransactionType.EXPENSE, LocalDate.now()));
        service.addTransaction(new Transaction("test52", 102, "test50", TransactionType.EXPENSE, LocalDate.now()));


        List<Transaction> res = service.getByCategory("test50");

        assertEquals(2, res.size());
        assertEquals("test50", res.getFirst().getDescription());
        assertEquals("test52", res.get(1).getDescription());
    }

}
