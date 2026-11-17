package service;

import model.Transaction;
import model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceEdgeCasesTest {
    private static Transaction transaction(String description, double amount, TransactionType type, int day) {
        return new Transaction(description, amount, "Food", type, LocalDate.of(2024, 1, day));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void invalidDeleteKeepsAllTransactions(int index) {
        TransactionService service = new TransactionService();
        Transaction a = transaction("a", 10, TransactionType.INCOME, 1);
        Transaction b = transaction("b", 5, TransactionType.EXPENSE, 2);
        service.addTransaction(a);
        service.addTransaction(b);
        assertFalse(service.deleteTransaction(index));
        assertEquals(List.of(a, b), service.getAllTransactions());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void invalidEditKeepsAllTransactions(int index) {
        TransactionService service = new TransactionService();
        Transaction a = transaction("a", 10, TransactionType.INCOME, 1);
        Transaction b = transaction("b", 5, TransactionType.EXPENSE, 2);
        service.addTransaction(a);
        service.addTransaction(b);
        assertFalse(service.editTransaction(index, transaction("replacement", 20, TransactionType.INCOME, 3)));
        assertEquals(List.of(a, b), service.getAllTransactions());
    }

    @Test
    void emptyServiceHasEmptyQueriesAndNoValidIndices() {
        TransactionService service = new TransactionService();
        assertTrue(service.getAllTransactions().isEmpty());
        assertTrue(service.getByCategory("Food").isEmpty());
        assertTrue(service.getByType(TransactionType.EXPENSE).isEmpty());
        assertTrue(service.getSortedByDate(true).isEmpty());
        assertTrue(service.getSortedByDate(false).isEmpty());
        assertFalse(service.deleteTransaction(0));
        assertFalse(service.editTransaction(0, transaction("a", 1, TransactionType.INCOME, 1)));
    }

    @Test
    void firstAndLastIndicesCanBeEditedAndDeletedWithoutChangingNeighbors() {
        TransactionService service = new TransactionService();
        Transaction a = transaction("a", 10, TransactionType.INCOME, 1);
        Transaction b = transaction("b", 5, TransactionType.EXPENSE, 2);
        Transaction c = transaction("c", 2, TransactionType.EXPENSE, 3);
        service.addTransaction(a);
        service.addTransaction(b);
        service.addTransaction(c);
        assertTrue(service.editTransaction(0, c));
        assertEquals(List.of(c, b, c), service.getAllTransactions());
        assertTrue(service.editTransaction(2, a));
        assertEquals(List.of(c, b, a), service.getAllTransactions());
        assertTrue(service.deleteTransaction(2));
        assertEquals(List.of(c, b), service.getAllTransactions());
        assertTrue(service.deleteTransaction(0));
        assertEquals(List.of(b), service.getAllTransactions());
    }

    @Test
    void balanceAccumulatesAllIncomeAndExpensesIncludingZeroAndRefunds() {
        TransactionService service = new TransactionService();
        service.addTransaction(transaction("salary", 100.25, TransactionType.INCOME, 1));
        service.addTransaction(transaction("bonus", 25.5, TransactionType.INCOME, 1));
        service.addTransaction(transaction("lunch", 10.75, TransactionType.EXPENSE, 2));
        service.addTransaction(transaction("refund", -3.25, TransactionType.EXPENSE, 3));
        service.addTransaction(transaction("zero", 0, TransactionType.INCOME, 3));
        assertEquals(118.25, service.getBalance());
    }

    @Test
    void categoryAndTypeFiltersPreserveOrderAndReturnIndependentLists() {
        TransactionService service = new TransactionService();
        Transaction a = transaction("a", 10, TransactionType.INCOME, 1);
        Transaction b = transaction("b", 5, TransactionType.EXPENSE, 2);
        Transaction c = new Transaction("c", 7, "Other", TransactionType.EXPENSE, a.getDate());
        service.addTransaction(a);
        service.addTransaction(b);
        service.addTransaction(c);
        List<Transaction> filtered = service.getByCategory("fOoD");
        assertEquals(List.of(a, b), filtered);
        assertEquals(List.of(b, c), service.getByType(TransactionType.EXPENSE));
        assertEquals(List.of(a), service.getByType(TransactionType.INCOME));
        assertTrue(service.getByCategory("missing").isEmpty());
        assertTrue(service.getByCategory(null).isEmpty());
        assertTrue(service.getByType(null).isEmpty());
        filtered.clear();
        service.getByType(TransactionType.EXPENSE).clear();
        assertEquals(List.of(a, b, c), service.getAllTransactions());
    }

    @Test
    void sortingIsStableAndDoesNotReorderOrExposeOriginalList() {
        TransactionService service = new TransactionService();
        Transaction later = transaction("later", 1, TransactionType.INCOME, 3);
        Transaction earlier = transaction("earlier", 2, TransactionType.EXPENSE, 1);
        Transaction sameDate = transaction("same date", 3, TransactionType.EXPENSE, 1);
        service.addTransaction(later);
        service.addTransaction(earlier);
        service.addTransaction(sameDate);
        assertEquals(List.of(earlier, sameDate, later), service.getSortedByDate(true));
        assertEquals(List.of(later, earlier, sameDate), service.getSortedByDate(false));
        service.getSortedByDate(true).clear();
        assertEquals(List.of(later, earlier, sameDate), service.getAllTransactions());
    }
}
