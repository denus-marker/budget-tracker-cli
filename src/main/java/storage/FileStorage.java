package storage;

import model.Transaction;
import model.TransactionType;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

public class FileStorage {
    private String filePath;

    public FileStorage(String filePath) {
        this.filePath = filePath;
    }

    public void save(List<Transaction> transactions){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Transaction t : transactions){
                String line = t.getDescription() + ";" + t.getAmount() + ";" + t.getCategory()
                        + ";" + t.getType() + ";" + t.getDate();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    public List<Transaction> load(){
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()){
            return transactions;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){

            String line;
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(";");
                String description = parts[0];
                double amount = Double.parseDouble(parts[1]);
                String cat = parts[2];
                TransactionType type = TransactionType.valueOf(parts[3]);
                LocalDate time = LocalDate.parse(parts[4]);

                transactions.add(new Transaction(description, amount, cat, type, time));
            }

        } catch (IOException e){
            System.out.println("Error loading: " + e.getMessage());
        }

        return transactions;
    }

    public void exportToCSV(List<Transaction> transactions, String csvPath){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath))){

            writer.write("Date,Type,Category,Amount,Description");
            writer.newLine();

            for (Transaction t : transactions){
                String line = t.getDate() + "," + t.getType()+ ","+ t.getCategory() + ","+
                        t.getAmount() + "," + t.getDescription();
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error exporting to CSV: " + e.getMessage());
        }
    }
}
