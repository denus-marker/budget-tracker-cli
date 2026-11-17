package storage;

import model.Transaction;
import model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.ConsoleSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("SYSTEM_STREAMS")
class FileStorageTest {
    @TempDir Path directory;

    private static List<Transaction> transactions() {
        return List.of(
                new Transaction("Výplata зарплата", 1000.25, "Work", TransactionType.INCOME, LocalDate.of(2024, 2, 29)),
                new Transaction("Lunch", -12.5, "Food", TransactionType.EXPENSE, LocalDate.of(2023, 12, 31)),
                new Transaction("", 0, "", TransactionType.EXPENSE, LocalDate.of(2024, 1, 1)));
    }

    @Test
    void savesExactDelimitedLinesAndLoadsEveryFieldInOrder() throws IOException {
        Path file = directory.resolve("transactions.txt");
        FileStorage storage = new FileStorage(file.toString());
        storage.save(transactions());
        assertEquals("Výplata зарплата;1000.25;Work;INCOME;2024-02-29\n"
                + "Lunch;-12.5;Food;EXPENSE;2023-12-31\n"
                + ";0.0;;EXPENSE;2024-01-01\n", normalized(file));
        assertEquals(transactions(), storage.load());
    }

    @Test
    void saveOverwritesOldContentAndEmptySaveTruncatesFile() throws IOException {
        Path file = directory.resolve("transactions.txt");
        Files.writeString(file, "old content that must disappear");
        FileStorage storage = new FileStorage(file.toString());
        storage.save(transactions().subList(1, 2));
        assertEquals("Lunch;-12.5;Food;EXPENSE;2023-12-31\n", normalized(file));
        storage.save(List.of());
        assertEquals("", Files.readString(file));
        assertEquals(List.of(), storage.load());
    }

    @Test
    void missingFileReturnsEmptyListWithoutCreatingFileOrPrintingError() {
        Path file = directory.resolve("missing.txt");
        try (ConsoleSession console = new ConsoleSession("")) {
            assertEquals(List.of(), new FileStorage(file.toString()).load());
            assertFalse(Files.exists(file));
            assertEquals("", console.output());
        }
    }

    @Test
    void missingFileCanStartAnEditableCollectionThatIsSavedLater() {
        Path file = directory.resolve("new-budget.txt");
        FileStorage storage = new FileStorage(file.toString());
        List<Transaction> loaded = storage.load();
        loaded.add(transactions().getFirst());
        assertFalse(Files.exists(file));
        assertEquals(List.of(), storage.load());
        storage.save(loaded);
        assertEquals(List.of(transactions().getFirst()), storage.load());
    }

    @Test
    void emptyFileLoadsAndLastLineDoesNotRequireNewline() throws IOException {
        Path file = directory.resolve("data.txt");
        Files.createFile(file);
        FileStorage storage = new FileStorage(file.toString());
        assertEquals(List.of(), storage.load());
        Files.writeString(file, "Lunch;-12.5;Food;EXPENSE;2023-12-31");
        assertEquals(transactions().subList(1, 2), storage.load());
    }

    @Test
    void exportsExactCsvHeaderAndEveryRecordAndOverwritesOldContent() throws IOException {
        Path csv = directory.resolve("export.csv");
        Files.writeString(csv, "obsolete");
        new FileStorage(directory.resolve("unused").toString()).exportToCSV(transactions(), csv.toString());
        assertEquals("Date,Type,Category,Amount,Description\n"
                + "2024-02-29,INCOME,Work,1000.25,Výplata зарплата\n"
                + "2023-12-31,EXPENSE,Food,-12.5,Lunch\n"
                + "2024-01-01,EXPENSE,,0.0,\n", normalized(csv));
    }

    @Test
    void emptyCsvExportContainsOnlyHeaderAndDoesNotChangeStorageFile() throws IOException {
        Path data = directory.resolve("data.txt");
        Path csv = directory.resolve("empty.csv");
        Files.writeString(data, "original");
        new FileStorage(data.toString()).exportToCSV(List.of(), csv.toString());
        assertEquals("Date,Type,Category,Amount,Description\n", normalized(csv));
        assertEquals("original", Files.readString(data));
    }

    @Test
    void savingToDirectoryReportsIoFailure() {
        try (ConsoleSession console = new ConsoleSession("")) {
            assertDoesNotThrow(() -> new FileStorage(directory.toString()).save(transactions()));
            assertError(console.output(), "Error saving: ");
        }
    }

    @Test
    void loadingDirectoryReportsIoFailureAndReturnsEmptyList() {
        try (ConsoleSession console = new ConsoleSession("")) {
            assertEquals(List.of(), new FileStorage(directory.toString()).load());
            assertError(console.output(), "Error loading: ");
        }
    }

    @Test
    void exportingToDirectoryReportsIoFailure() {
        try (ConsoleSession console = new ConsoleSession("")) {
            assertDoesNotThrow(() -> new FileStorage("unused").exportToCSV(transactions(), directory.toString()));
            assertError(console.output(), "Error exporting to CSV: ");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-number", ""})
    void malformedAmountsAreReportedAsNumberFormatExceptions(String amount) throws IOException {
        Path file = directory.resolve("bad.txt");
        Files.writeString(file, "Lunch;" + amount + ";Food;EXPENSE;2024-01-01");
        assertThrows(NumberFormatException.class, () -> new FileStorage(file.toString()).load());
    }

    @Test
    void malformedRecordWithoutAllFieldsThrowsInsteadOfReturningPartialData() throws IOException {
        Path file = directory.resolve("bad.txt");
        Files.writeString(file, "Lunch");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new FileStorage(file.toString()).load());
    }

    @Test
    void unknownTransactionTypeThrows() throws IOException {
        Path file = directory.resolve("bad.txt");
        Files.writeString(file, "Lunch;10;Food;UNKNOWN;2024-01-01");
        assertThrows(IllegalArgumentException.class, () -> new FileStorage(file.toString()).load());
    }

    @Test
    void impossibleDateThrows() throws IOException {
        Path file = directory.resolve("bad.txt");
        Files.writeString(file, "Lunch;10;Food;EXPENSE;2024-02-30");
        assertThrows(DateTimeParseException.class, () -> new FileStorage(file.toString()).load());
    }

    private static String normalized(Path path) throws IOException {
        return Files.readString(path).replace("\r\n", "\n");
    }

    private static void assertError(String output, String prefix) {
        assertTrue(output.startsWith(prefix), output);
        assertTrue(output.substring(prefix.length()).strip().length() > 0, "The I/O error must include its reason");
        assertTrue(output.endsWith("\n"), output);
    }
}
