import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import support.ConsoleSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.*;

@ResourceLock("SYSTEM_STREAMS")
@ResourceLock("TRANSACTIONS_FILE")
class MainTest {
    @Test
    void entryPointLoadsShowsAndPersistsTransactionsThenExits() throws Exception {
        // Main hardcodes this path. Restore any pre-existing user file, including its timestamp.
        Path file = Path.of("transactions.txt");
        boolean existed = Files.exists(file);
        byte[] original = existed ? Files.readAllBytes(file) : null;
        FileTime modified = existed ? Files.getLastModifiedTime(file) : null;
        try {
            String record = "Salary;100.5;Work;INCOME;2024-02-29" + System.lineSeparator();
            Files.writeString(file, record);
            try (ConsoleSession console = new ConsoleSession("2\n3\n10\n")) {
                new Main();
                Main.main(new String[0]);
                assertTrue(console.output().contains("2024-02-29 | INCOME | Work | 100.5 | Salary\n"));
                assertTrue(console.output().contains("Account balance: 100.5\n"));
                assertTrue(console.output().endsWith("Bye!\n"));
            }
            assertEquals(record, Files.readString(file));
        } finally {
            if (existed) {
                Files.write(file, original);
                Files.setLastModifiedTime(file, modified);
            } else {
                Files.deleteIfExists(file);
            }
        }
    }
}
