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
            System.out.println("5. Delete transaction");
            System.out.println("6. Edit transaction");
            System.out.println("7. Exit");
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
                    deleteTransaction(scanner,service);
                    break;
                case "6":
                    editTransaction(scanner, service);
                    break;
                case "7":
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

    private static void deleteTransaction(Scanner scanner, TransactionService service) {
        List<Transaction> transactions = service.getAllTransactions();
        if (transactions.isEmpty()) {
            System.out.println("Empty for now.");
            return;
        }
        for (int i = 0; i < transactions.size(); i++) {
            System.out.println( (i+1) + ". " + transactions.get(i));
        }

        System.out.println("Enter the transaction number you want to delete: ");
        int index;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e){
            System.out.println("Invalid input.");
            return;
        }
        boolean deleted = service.deleteTransaction(index);
        if (deleted) System.out.println("Transaction was deleted.");
        else System.out.println("No transaction with that number.");
    }

    private static void editTransaction(Scanner scanner, TransactionService service) {
        List<Transaction> transactions = service.getAllTransactions();

        if (transactions.isEmpty()) {
            System.out.println("Empty for now.");
            return;
        }

        for (int i = 0; i < transactions.size(); i++) {
            System.out.println((i + 1) + ". " + transactions.get(i));
        }

        System.out.println("Enter the transaction number you want to edit: ");
        int index;
        try {
            index = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (index < 0 || index >= transactions.size()) {
            System.out.println("No transaction with that number.");
            return;
        }

        Transaction old = transactions.get(index);

        System.out.println("Choose what you want to edit.");
        System.out.println("1. Description");
        System.out.println("2. Amount");
        System.out.println("3. Category");
        System.out.println("4. Type");
        System.out.println("5. Date");

        String choice = scanner.nextLine();
        Transaction updated;

        switch (choice) {
            case "1":
                System.out.println("Enter a new description: ");
                String newDescription = scanner.nextLine();
                updated = new Transaction(newDescription, old.getAmount(), old.getCategory(), old.getType(), old.getDate());
                break;
            case "2":
                System.out.println("Enter a new amount: ");
                double newAmount;
                try {
                    newAmount = Double.parseDouble(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount.");
                    return;
                }
                updated = new Transaction(old.getDescription(), newAmount, old.getCategory(), old.getType(), old.getDate());
                break;
            case "3":
                System.out.println("Enter a new category: ");
                String newCategory = scanner.nextLine();
                updated = new Transaction(old.getDescription(), old.getAmount(), newCategory, old.getType(), old.getDate());
                break;
            case "4":
                System.out.println("New type (1 - income, 2 - expense): ");
                String typeChoice = scanner.nextLine();
                TransactionType newType = typeChoice.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE;
                updated = new Transaction(old.getDescription(), old.getAmount(), old.getCategory(), newType, old.getDate());
                break;
            case "5":
                System.out.println("Enter a new date (format: YYYY-MM-DD)");
                String dateInput = scanner.nextLine();
                LocalDate newDate;
                try {
                    newDate = LocalDate.parse(dateInput);
                } catch (Exception e){
                    System.out.println("Invalid date format.");
                    return;
                }
                updated = new Transaction(old.getDescription(), old.getAmount(), old.getCategory(), old.getType(), newDate);
                break;
            default:
                System.out.println("Wrong choice.");
                return;
        }

        service.editTransaction(index, updated);
        System.out.println("Transaction updated.");
    }
}
