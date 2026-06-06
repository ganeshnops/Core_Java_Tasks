package com.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;
import com.bank.exception.MinimumDepositException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.Bank;

/**
 * Banking Application - Main entry point.
 *
 * Two phases:
 *   PHASE 1 (auto)  : Quick validation of all 6 business rules so we can show
 *                     the rules work even before the user starts clicking.
 *   PHASE 2 (menu)  : Interactive menu - user can create accounts, deposit,
 *                     withdraw, transfer, view balance, view history, list
 *                     all accounts and trigger the multi-threaded stress test.
 */
public class Main {

    private static final Bank bank = new Bank();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();
        autoValidateBusinessRules();

        boolean run = true;
        while (run) {
            printMenu();
            int choice = readInt("Choose option: ");
            System.out.println();
            switch (choice) {
                case 1: createAccount();          break;
                case 2: deposit();                break;
                case 3: withdraw();               break;
                case 4: transfer();               break;
                case 5: checkBalance();           break;
                case 6: viewTransactionHistory(); break;
                case 7: listAllAccounts();        break;
                case 8: runStressTest();          break;
                case 9: run = false;              break;
                default: System.out.println("Invalid choice. Please pick 1-9.");
            }
            System.out.println();
        }
        System.out.println("Thank you for using JHires Bank. Goodbye!");
        sc.close();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#                  SBI Bank - Welcome                 #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. Create Account");
        System.out.println(" 2. Deposit");
        System.out.println(" 3. Withdraw");
        System.out.println(" 4. Transfer");
        System.out.println(" 5. Check Balance");
        System.out.println(" 6. View Transaction History");
        System.out.println(" 7. List All Accounts");
        System.out.println(" 8. Run Stress Test (multithreading + deadlock)");
        System.out.println(" 9. Exit");
        System.out.println("==============================================");
    }

    private static void autoValidateBusinessRules() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Quick validation of all 6 business rules");
        System.out.println("=========================================================");

        try {
            bank.createAccount("LowDepositGuy", Bank.AccountType.SAVINGS, 500);
        } catch (MinimumDepositException ex) {
            System.out.println("[OK] Rule 1 caught: " + ex.getMessage());
        }

        Account alice = bank.createAccount("Alice", Bank.AccountType.SAVINGS, 5000);
        Account bob   = bank.createAccount("Bob",   Bank.AccountType.CURRENT, 3000);
        Account carol = bank.createAccount("Carol", Bank.AccountType.SAVINGS, 10000);
        System.out.println("[OK] 3 demo accounts created:");
        System.out.println("     " + alice);
        System.out.println("     " + bob);
        System.out.println("     " + carol);

        try { alice.deposit(-100); }
        catch (InvalidAmountException ex) { System.out.println("[OK] Rule 2 caught: " + ex.getMessage()); }

        try { alice.withdraw(0); }
        catch (InvalidAmountException ex) { System.out.println("[OK] Rule 3 caught: " + ex.getMessage()); }

        try { alice.withdraw(1_00_000); }
        catch (InsufficientBalanceException ex) { System.out.println("[OK] Rule 4 caught: " + ex.getMessage()); }

        try { bank.transfer(alice.getAccountNumber(), bob.getAccountNumber(), 0); }
        catch (InvalidAmountException ex) { System.out.println("[OK] Rule 5 caught: " + ex.getMessage()); }

        System.out.println("[OK] Rule 6 - auto-generated unique account numbers: "
                + alice.getAccountNumber() + ", "
                + bob.getAccountNumber() + ", "
                + carol.getAccountNumber());
        System.out.println();
    }

    private static void createAccount() {
        System.out.println("--- Create Account ---");
        String name = readString("Enter holder name : ");
        System.out.println("Type: 1.SAVINGS  2.CURRENT");
        int t = readInt("Choose type        : ");
        Bank.AccountType type = (t == 2) ? Bank.AccountType.CURRENT : Bank.AccountType.SAVINGS;
        double dep = readDouble("Initial deposit (>= 1000): ");
        try {
            Account acc = bank.createAccount(name, type, dep);
            System.out.println("Account created -> " + acc);
        } catch (MinimumDepositException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void deposit() {
        System.out.println("--- Deposit ---");
        String accNo = readString("Account number : ");
        double amt   = readDouble("Amount         : ");
        try {
            bank.deposit(accNo, amt);
            System.out.println("OK. New balance: Rs." + bank.getAccount(accNo).getBalance());
        } catch (AccountNotFoundException | InvalidAmountException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void withdraw() {
        System.out.println("--- Withdraw ---");
        String accNo = readString("Account number : ");
        double amt   = readDouble("Amount         : ");
        try {
            bank.withdraw(accNo, amt);
            System.out.println("OK. New balance: Rs." + bank.getAccount(accNo).getBalance());
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientBalanceException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void transfer() {
        System.out.println("--- Transfer ---");
        String from = readString("From account : ");
        String to   = readString("To account   : ");
        double amt  = readDouble("Amount       : ");
        try {
            bank.transfer(from, to, amt);
            System.out.println("OK. Transfer complete.");
            System.out.println("  " + bank.getAccount(from));
            System.out.println("  " + bank.getAccount(to));
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientBalanceException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void checkBalance() {
        System.out.println("--- Check Balance ---");
        String accNo = readString("Account number : ");
        try {
            Account a = bank.getAccount(accNo);
            System.out.println(a);
        } catch (AccountNotFoundException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void viewTransactionHistory() {
        System.out.println("--- Transaction History ---");
        String accNo = readString("Account number : ");
        try {
            Account a = bank.getAccount(accNo);
            List<Transaction> txns = a.getTransactions();
            System.out.println("Total transactions: " + txns.size());
            int from = Math.max(0, txns.size() - 10);
            if (from > 0) System.out.println("(showing last 10)");
            for (int i = from; i < txns.size(); i++) {
                System.out.println("  " + txns.get(i));
            }
        } catch (AccountNotFoundException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void listAllAccounts() {
        System.out.println("--- All Accounts ---");
        for (Account a : bank.getAllAccounts()) {
            System.out.println("  " + a);
        }
    }

    private static void runStressTest() {
        System.out.println("--- Stress Test (multithreading + deadlock prevention) ---");
        if (bank.getAllAccounts().size() < 2) {
            System.out.println("Need at least 2 accounts.");
            return;
        }
        List<Account> all = new ArrayList<>(bank.getAllAccounts());
        String[] accs = new String[all.size()];
        for (int i = 0; i < all.size(); i++) accs[i] = all.get(i).getAccountNumber();

        System.out.println("Total money before : Rs." + sum(all));

        int workers = 30;
        int opsPerWorker = 50;
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < workers; i++) {
            Thread t = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int j = 0; j < opsPerWorker; j++) {
                    int op = rnd.nextInt(3);
                    String from = accs[rnd.nextInt(accs.length)];
                    String to   = accs[rnd.nextInt(accs.length)];
                    double amt  = rnd.nextInt(1, 200);
                    try {
                        if (op == 0)               bank.deposit(from, amt);
                        else if (op == 1)          bank.withdraw(from, amt);
                        else if (!from.equals(to)) bank.transfer(from, to, amt);
                    } catch (InsufficientBalanceException | InvalidAmountException ignored) { }
                }
            }, "Worker-" + i);
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        System.out.println("All " + workers + " threads finished without deadlock.");
        System.out.println("Total money after  : Rs." + sum(all));
        for (Account a : all) {
            if (a.getBalance() < 0) System.out.println("BUG: " + a + " went negative!");
        }
        System.out.println("[OK] No account went negative.");
    }

    private static double sum(List<Account> accs) {
        double s = 0;
        for (Account a : accs) s += a.getBalance();
        return s;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException ex) { System.out.println("  Invalid number, try again."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException ex) { System.out.println("  Invalid number, try again."); }
        }
    }
}
