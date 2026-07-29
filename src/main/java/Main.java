import service.TransactionService;
import storage.FileStorage;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        TransactionService service = new TransactionService();
        FileStorage storage = new FileStorage("transactions.txt");
        ConsoleUI consoleUI = new ConsoleUI(service, storage);
        consoleUI.run();
    }
}
