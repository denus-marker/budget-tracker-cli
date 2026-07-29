package service;

import model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
