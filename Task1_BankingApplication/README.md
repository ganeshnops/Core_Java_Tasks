# Task 1 - Banking Application (Core Java)

Multi-account banking app with concurrent deposit / withdraw / transfer.
Uses OOP, Collections, Exception Handling, Multithreading, Synchronization and
Deadlock Prevention.

## Business Rules
1. Initial deposit must be at least Rs.1000.
2. Deposit amount must be positive.
3. Withdrawal amount must be positive.
4. Balance should never become negative.
5. Transfer amount must be positive.
6. Account numbers should be unique and auto-generated.

## Project structure
```
Task1_BankingApplication/
└── src/
    └── com/bank/
        ├── Main.java                       (demo + tests)
        ├── exception/
        │   ├── AccountNotFoundException.java
        │   ├── InsufficientBalanceException.java
        │   ├── InvalidAmountException.java
        │   └── MinimumDepositException.java
        ├── model/
        │   ├── Account.java                (abstract)
        │   ├── SavingsAccount.java
        │   ├── CurrentAccount.java
        │   └── Transaction.java
        └── service/
            └── Bank.java                   (main service)
```

## How to run in Eclipse
1. Eclipse -> File -> New -> Java Project.
2. Project name: `Task1_BankingApplication`. UNCHECK "Use default location"
   and point it at `D:\GANESH\Ops\Rjay\Tasks\Core_Java_Tasks\Task1_BankingApplication`.
3. Eclipse will pick up the `src/com/bank/...` packages automatically.
4. Right-click `Main.java` -> Run As -> Java Application.

## Concepts demonstrated
- **OOP - Encapsulation**: `Account.balance` is `private`. Reads/writes go
  through synchronized methods only.
- **OOP - Abstraction**: `Account` is abstract; `SavingsAccount` and
  `CurrentAccount` extend it.
- **Collections**: `ConcurrentHashMap<String, Account>` in `Bank`; `ArrayList`
  of `Transaction` per account.
- **Exception Handling**: 4 custom unchecked exceptions for the 6 rules.
- **Multithreading**: `Main` spawns 30 worker threads doing 50 random ops each.
- **Synchronization**: every mutation of an `Account` holds its
  `ReentrantLock`.
- **Deadlock Prevention**: `Bank.transfer` always locks the account with the
  smaller account number first, so two threads doing A->B and B->A can never
  hold-and-wait on each other.
