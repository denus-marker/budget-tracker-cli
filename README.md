# Budget Tracker CLI

A console application for tracking personal income and expenses, built in Java.

This project was built as a hands-on exercise in core Java concepts — OOP, collections,
file I/O, unit testing, and application architecture (separation of concerns between
data, business logic, storage, and UI layers).

## Features

- Add income or expense transactions with description, amount, category, and date
- View all recorded transactions
- Calculate current balance (income minus expenses)
- Filter transactions by category
- Filter transactions by type (income / expense)
- Edit existing transactions (any field)
- Delete transactions
- Sort transactions by date (ascending / descending)
- Export transactions to CSV
- Persist data between sessions (saved to a local file)
- Covered by unit tests (JUnit 5)

## Tech Stack

- Java
- Maven
- JUnit 5

## Project Structure

```
src/main/java/
├── Main.java                    // entry point
├── model/
│   ├── Transaction.java         // transaction data model
│   └── TransactionType.java     // enum: INCOME / EXPENSE
├── service/
│   └── TransactionService.java  // business logic (CRUD, filtering, sorting, balance)
├── storage/
│   └── FileStorage.java         // save/load transactions, CSV export
└── ui/
    └── ConsoleUI.java           // console menu and user interaction

src/test/java/
└── service/
    └── TransactionServiceTest.java  // unit tests for business logic
```

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/denus-marker/budget-tracker-cli.git
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
5. Delete transaction
6. Edit transaction
7. Sort by date
8. Export to CSV
9. Filter by type
10. Exit
```

Data is automatically saved to `transactions.txt` on exit and loaded back on the next run.

## Running Tests

```bash
mvn test
```

## Possible Future Improvements

- Switch storage to a real database (SQLite)
- Monthly/date-range filtering
- Basic statistics (spending by category, monthly totals)
- Multi-user support