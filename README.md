# Budget Tracker CLI

A simple console application for tracking personal income and expenses, built in Java.

This project was built as a hands-on exercise in core Java concepts — OOP, collections, 
file I/O, and basic application architecture (separation of concerns between data, 
business logic, storage, and UI layers).

## Features

- Add income or expense transactions with description, amount, category, and date
- View all recorded transactions
- Calculate current balance (income minus expenses)
- Filter transactions by category
- Persist data between sessions (saved to a local file)
- Delete transactions

## Tech Stack

- Java
- Maven

## Project Structure

```
src/main/java/
├── Main.java                    // entry point
├── model/
│   ├── Transaction.java         // transaction data model
│   └── TransactionType.java     // enum: INCOME / EXPENSE
├── service/
│   └── TransactionService.java  // business logic (add, filter, balance calculation)
├── storage/
│   └── FileStorage.java         // save/load transactions to/from a file
└── ui/
    └── ConsoleUI.java           // console menu and user interaction
```

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/budget-tracker-cli.git
   ```
2. Open the project in IntelliJ IDEA (or any IDE with Maven support)
3. Run `Main.java`

## Usage

On launch, you'll see a menu:

```
=== Budget Tracker ===
1. Add transaction
2. Show all transactions
3. Show account balance
4. Filter by category
5. Exit
```

Data is automatically saved to `transactions.txt` on exit and loaded back on the next run.

## Possible Future Improvements

- Delete/edit existing transactions
- Export to CSV
- Unit tests (JUnit)
- Monthly/date-range filtering
- Switch storage to a real database (SQLite)
