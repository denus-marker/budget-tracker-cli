package ui;

import model.Transaction;
import model.TransactionType;
import service.TransactionService;
import storage.FileStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private TransactionService service;
    private FileStorage storage;
    private Scanner scanner;

    public ConsoleUI(TransactionService service, FileStorage storage) {
        this.service = service;
        this.storage = storage;
        this.scanner = new Scanner(System.in);
    }

    public void run(){
        boolean running = true;

        List<Transaction> loaded = storage.load();
        for (Transaction t : loaded) {
            service.addTransaction(t);
        }

        while(running){
            System.out.println("\n=== Budget Tracker ===");
            System.out.println("1. Add transaction");
            System.out.println("2. Show all transactions");
            System.out.println("3. Show account balance");
            System.out.println("4. Filter by category");
            System.out.println("5. Exit");
            System.out.println("Please choose: ");

            String choice = scanner.nextLine();

            switch (choice){
                case "1":
                    addTransaction(scanner,service);
                    break;
                case "2":
                    showAll(service);
                    break;
                case "3":
                    System.out.println("Account balance: " + service.getBalance());
                    break;
                case "4":
                    filterByCategory(scanner, service);
                    break;
                case "5":
                    storage.save(service.getAllTransactions());
                    running = false;
                    System.out.println("Bye!");
                    break;
                default:
                    System.out.println("Wrong choice, try again");
            }
        }
    }

    private static void addTransaction(Scanner scanner, TransactionService service){
        System.out.println("Description: ");
        String description = scanner.nextLine();

        System.out.print("Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. The transaction was not added.");
            return;
        }

        System.out.println("Category: ");
        String category = scanner.nextLine();

        System.out.println("TYPE (1 - income, 2 - expense) : ");
        String typeChoice = scanner.nextLine();
        TransactionType type = typeChoice.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE;

        Transaction transaction = new Transaction(description, amount, category, type, LocalDate.now());
        service.addTransaction(transaction);

        System.out.println("Added.");
    }

    private static void showAll(TransactionService service){
        List<Transaction> transactions = service.getAllTransactions();
        if (transactions.isEmpty()){
            System.out.println("Empty for now.");
            return;
        }else {
            for (Transaction t : transactions){
                System.out.println(t);
            }
        }
    }

    private static void filterByCategory(Scanner scanner, TransactionService service){
        System.out.println("Enter a category: ");
        String category = scanner.nextLine();
        List<Transaction> result = service.getByCategory(category);
        if (result.isEmpty()){
            System.out.println("Empty for now.");
            return;
        }else {
            for (Transaction t : result){
                System.out.println(t);
            }
        }
    }
}
