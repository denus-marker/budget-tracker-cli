package ui;

import model.Transaction;
import model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import service.TransactionService;
import storage.FileStorage;
import support.ConsoleSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("SYSTEM_STREAMS")
class ConsoleUITest {
    private static final String MENU = "\n=== Budget Tracker ===\n"
            + "1. Add transaction\n2. Show all transactions\n3. Show account balance\n"
            + "4. Filter by category\n5. Delete transaction\n6. Edit transaction\n"
            + "7. Sort by date\n8. Export to CSV\n9. Filter by type\n10. Exit\nPlease choose: \n";
    private static final LocalDate DATE = LocalDate.of(2024, 2, 29);
    private static final Transaction INCOME = new Transaction("Salary", 100.5, "Work", TransactionType.INCOME, DATE);
    private static final Transaction EXPENSE = new Transaction("Lunch", 12.25, "Food", TransactionType.EXPENSE, DATE.minusDays(1));
    private static final String ADD_PROMPTS = "Description: \nAmount: Category: \nTYPE (1 - income, 2 - expense) : \n";
    private static final String DELETE_PROMPT = "Enter the transaction number you want to delete: \n";
    private static final String EDIT_PROMPT = "Enter the transaction number you want to edit: \n";
    private static final String EDIT_MENU = "Choose what you want to edit.\n1. Description\n2. Amount\n3. Category\n4. Type\n5. Date\n";
    private static final String SORT_PROMPT = "Please choose: \n 1 - in ascending order. \n 2 - in descending order\n";

    @Test
    void exitLoadsAndSavesExistingTransactionsAndPrintsWholeMenu() {
        RecordingStorage storage = new RecordingStorage(List.of(INCOME, EXPENSE));
        TransactionService service = new TransactionService();
        try (ConsoleSession console = new ConsoleSession("10\n")) {
            new ConsoleUI(service, storage).run();
            assertEquals(MENU + "Bye!\n", console.output());
        }
        assertEquals(List.of(INCOME, EXPENSE), service.getAllTransactions());
        assertEquals(List.of(INCOME, EXPENSE), storage.saved);
        assertEquals(1, storage.loads);
        assertEquals(1, storage.saves);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "11", "abc"})
    void unknownMenuChoiceReportsErrorAndAllowsExit(String choice) {
        run(List.of(), choice + "\n", "Wrong choice, try again\n");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "other"})
    void addsTransactionWithSelectedTypeAndCurrentDate(String typeChoice) {
        LocalDate before = LocalDate.now();
        Result result = run(List.of(), "1\nCoffee\n2.5\nFood\n" + typeChoice + "\n", ADD_PROMPTS + "Added.\n");
        LocalDate after = LocalDate.now();
        assertEquals(1, result.transactions().size());
        Transaction actual = result.transactions().getFirst();
        assertTrue(actual.getDate().equals(before) || actual.getDate().equals(after));
        assertEquals(new Transaction("Coffee", 2.5, "Food",
                typeChoice.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE, actual.getDate()), actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "1,5"})
    void invalidAmountDoesNotAddTransaction(String amount) {
        Result result = run(List.of(INCOME), "1\nBad\n" + amount + "\n",
                "Description: \nAmount: Invalid amount. The transaction was not added.\n");
        assertEquals(List.of(INCOME), result.transactions());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "4\nFood", "5", "6", "7", "8", "9"})
    void commandsHandleAnEmptyAccount(String command) {
        String prompt = command.startsWith("4") ? "Enter a category: \n" : "";
        Result result = run(List.of(), command + "\n", prompt + "Empty for now.\n");
        assertEquals(List.of(), result.transactions());
        assertEquals(0, result.storage().exports);
    }

    @Test
    void showsAllTransactionsInInsertionOrder() {
        run(List.of(INCOME, EXPENSE), "2\n", rows(INCOME, EXPENSE));
    }

    @Test
    void showsCalculatedAccountBalance() {
        run(List.of(INCOME, EXPENSE), "3\n", "Account balance: 88.25\n");
    }

    @Test
    void showsZeroBalanceForEmptyAccount() {
        run(List.of(), "3\n", "Account balance: 0.0\n");
    }

    @Test
    void filtersCategoryIgnoringCaseAndPrintsEveryMatch() {
        Transaction other = new Transaction("Dinner", 20, "FOOD", TransactionType.EXPENSE, DATE);
        run(List.of(INCOME, EXPENSE, other), "4\nfOoD\n", "Enter a category: \n" + rows(EXPENSE, other));
    }

    @Test
    void categoryWithNoMatchesShowsEmptyMessage() {
        run(List.of(INCOME), "4\nmissing\n", "Enter a category: \nEmpty for now.\n");
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "2, 1"})
    void deletesFirstOrLastUsingOneBasedNumbers(int number, int index) {
        Result result = run(List.of(INCOME, EXPENSE), "5\n" + number + "\n",
                numbered() + DELETE_PROMPT + "Transaction was deleted.\n");
        assertEquals(index == 0 ? List.of(EXPENSE) : List.of(INCOME), result.transactions());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "3"})
    void nonexistentDeleteNumberLeavesAccountUnchanged(String number) {
        Result result = run(List.of(INCOME, EXPENSE), "5\n" + number + "\n",
                numbered() + DELETE_PROMPT + "No transaction with that number.\n");
        assertEquals(List.of(INCOME, EXPENSE), result.transactions());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "2147483648"})
    void nonIntegerDeleteNumberLeavesAccountUnchanged(String number) {
        Result result = run(List.of(INCOME, EXPENSE), "5\n" + number + "\n",
                numbered() + DELETE_PROMPT + "Invalid input.\n");
        assertEquals(List.of(INCOME, EXPENSE), result.transactions());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "3"})
    void nonexistentEditNumberLeavesAccountUnchanged(String number) {
        Result result = run(List.of(INCOME, EXPENSE), "6\n" + number + "\n",
                numbered() + EDIT_PROMPT + "No transaction with that number.\n");
        assertEquals(List.of(INCOME, EXPENSE), result.transactions());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "2147483648"})
    void nonIntegerEditNumberLeavesAccountUnchanged(String number) {
        Result result = run(List.of(INCOME, EXPENSE), "6\n" + number + "\n",
                numbered() + EDIT_PROMPT + "Invalid input.\n");
        assertEquals(List.of(INCOME, EXPENSE), result.transactions());
    }

    @Test
    void editsDescriptionAndPreservesOtherFields() {
        assertEdited("1\nNew salary\n", "Enter a new description: \n",
                new Transaction("New salary", 100.5, "Work", TransactionType.INCOME, DATE));
    }

    @Test
    void editsAmountAndPreservesOtherFields() {
        assertEdited("2\n0\n", "Enter a new amount: \n",
                new Transaction("Salary", 0, "Work", TransactionType.INCOME, DATE));
    }

    @Test
    void editsCategoryAndPreservesOtherFields() {
        assertEdited("3\nBonus\n", "Enter a new category: \n",
                new Transaction("Salary", 100.5, "Bonus", TransactionType.INCOME, DATE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "other"})
    void editsTypeAndPreservesOtherFields(String type) {
        assertEdited("4\n" + type + "\n", "New type (1 - income, 2 - expense): \n",
                new Transaction("Salary", 100.5, "Work", type.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE, DATE));
    }

    @Test
    void editsDateAndPreservesOtherFields() {
        assertEdited("5\n2025-01-01\n", "Enter a new date (format: YYYY-MM-DD)\n",
                new Transaction("Salary", 100.5, "Work", TransactionType.INCOME, LocalDate.of(2025, 1, 1)));
    }

    @Test
    void editsLastTransactionUsingOneBasedIndex() {
        Result result = run(List.of(INCOME, EXPENSE), "6\n2\n1\nDinner\n",
                numbered() + EDIT_PROMPT + EDIT_MENU + "Enter a new description: \nTransaction updated.\n");
        assertEquals(List.of(INCOME, new Transaction("Dinner", 12.25, "Food", TransactionType.EXPENSE, DATE.minusDays(1))), result.transactions());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", ""})
    void invalidEditedAmountDoesNotChangeTransaction(String amount) {
        assertRejectedEdit("2\n" + amount + "\n", "Enter a new amount: \nInvalid amount.\n");
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad-date", "2024-02-30", ""})
    void invalidEditedDateDoesNotChangeTransaction(String date) {
        assertRejectedEdit("5\n" + date + "\n", "Enter a new date (format: YYYY-MM-DD)\nInvalid date format.\n");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "6", ""})
    void unknownEditFieldDoesNotChangeTransaction(String choice) {
        assertRejectedEdit(choice + "\n", "Wrong choice.\n");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "other"})
    void sortPrintsSelectedOrderAndKeepsStoredOrder(String order) {
        String expectedRows = order.equals("1") ? rows(EXPENSE, INCOME) : rows(INCOME, EXPENSE);
        Result result = run(List.of(INCOME, EXPENSE), "7\n" + order + "\n",
                SORT_PROMPT + (order.equals("other") ? "Wrong option!\n" : "") + expectedRows);
        assertEquals(List.of(INCOME, EXPENSE), result.transactions());
    }

    @Test
    void exportPassesAllTransactionsAndFixedCsvPathToStorage() {
        Result result = run(List.of(INCOME, EXPENSE), "8\n", "Exported to transactions_export.csv\n");
        assertEquals(1, result.storage().exports);
        assertEquals("transactions_export.csv", result.storage().csvPath);
        assertEquals(List.of(INCOME, EXPENSE), result.storage().exported);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2"})
    void filtersSelectedType(String type) {
        run(List.of(INCOME, EXPENSE), "9\n" + type + "\n",
                "1 - Income, 2 - Expense\n" + (type.equals("1") ? rows(INCOME) : rows(EXPENSE)));
    }

    @Test
    void selectedTypeWithNoMatchesHasSpecificMessage() {
        run(List.of(INCOME), "9\n2\n", "1 - Income, 2 - Expense\nNo transactions of this type.\n");
    }

    @Test
    void invalidTypeChoiceShowsError() {
        run(List.of(INCOME), "9\n3\n", "1 - Income, 2 - Expense\nWrong option!\n");
    }

    private static void assertEdited(String editInput, String prompt, Transaction expected) {
        Result result = run(List.of(INCOME, EXPENSE), "6\n1\n" + editInput,
                numbered() + EDIT_PROMPT + EDIT_MENU + prompt + "Transaction updated.\n");
        assertEquals(List.of(expected, EXPENSE), result.transactions());
    }

    private static void assertRejectedEdit(String editInput, String expectedOutput) {
        Result result = run(List.of(INCOME, EXPENSE), "6\n1\n" + editInput,
                numbered() + EDIT_PROMPT + EDIT_MENU + expectedOutput);
        assertEquals(List.of(INCOME, EXPENSE), result.transactions());
    }

    private static Result run(List<Transaction> initial, String input, String expectedActionOutput) {
        TransactionService service = new TransactionService();
        RecordingStorage storage = new RecordingStorage(initial);
        try (ConsoleSession console = new ConsoleSession(input + "10\n")) {
            new ConsoleUI(service, storage).run();
            assertEquals(MENU + expectedActionOutput + MENU + "Bye!\n", console.output());
        }
        assertEquals(1, storage.loads);
        assertEquals(1, storage.saves);
        assertEquals(service.getAllTransactions(), storage.saved);
        return new Result(List.copyOf(service.getAllTransactions()), storage);
    }

    private static String rows(Transaction... transactions) {
        StringBuilder output = new StringBuilder();
        for (Transaction transaction : transactions) output.append(transaction).append('\n');
        return output.toString();
    }

    private static String numbered() {
        return "1. " + INCOME + "\n2. " + EXPENSE + "\n";
    }

    private record Result(List<Transaction> transactions, RecordingStorage storage) { }

    /** Keeps UI tests independent of disk and records the public storage interactions. */
    private static class RecordingStorage extends FileStorage {
        private final List<Transaction> initial;
        private List<Transaction> saved;
        private List<Transaction> exported;
        private String csvPath;
        private int loads;
        private int saves;
        private int exports;

        RecordingStorage(List<Transaction> initial) {
            super("unused-by-test-double");
            this.initial = List.copyOf(initial);
        }

        @Override public List<Transaction> load() {
            loads++;
            return new ArrayList<>(initial);
        }

        @Override public void save(List<Transaction> transactions) {
            saves++;
            saved = List.copyOf(transactions);
        }

        @Override public void exportToCSV(List<Transaction> transactions, String path) {
            exports++;
            exported = List.copyOf(transactions);
            csvPath = path;
        }
    }
}
