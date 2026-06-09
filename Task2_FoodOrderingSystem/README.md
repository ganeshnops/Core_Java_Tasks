# Task 2 - Food Ordering System (Core Java)

Simplified food-ordering system covering 60 business rules from the assignment.

## Tech
- Core Java 11+
- Concurrency: `Semaphore`, `ReentrantLock`, `AtomicInteger`, `AtomicLong`, `ConcurrentHashMap`
- Idempotent payments
- Status machine with valid transitions only

## Project Structure
```
Task2_FoodOrderingSystem/
└── src/com/food/
    ├── Main.java
    ├── enums/                  (6 enums)
    ├── exception/              (parent + 10 specific)
    ├── model/                  (10 entity classes)
    └── service/                (9 services)
```

## How to run (Eclipse)
1. Unzip + copy to `D:\GANESH\Ops\Rjay\Tasks\Core_Java_Tasks\Task2_FoodOrderingSystem`
2. Eclipse → File → New → Java Project
3. Project name: `Task2_FoodOrderingSystem`, UNCHECK default location, browse to the folder
4. JRE: Java 11+
5. Right-click `Main.java` → Run As → Java Application
6. Type menu numbers in the Eclipse Console

## What the demo does
1. **PHASE 1** boots 2 restaurants, 9 menu items, 3 customers (1 with unverified mobile), 3 delivery partners, 3 coupons (1 expired).
2. **PHASE 2** runs an automatic happy-path order for Alice (C1) all the way to DELIVERED + review.
3. **Menu** lets you place orders, advance status, cancel, run a concurrent stress test, view reports, submit reviews.
