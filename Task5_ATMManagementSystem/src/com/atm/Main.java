package com.atm;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import com.atm.enums.AccountType;
import com.atm.enums.Denomination;
import com.atm.exception.ATMException;
import com.atm.model.ATM;
import com.atm.model.ATMCard;
import com.atm.model.Account;
import com.atm.model.Customer;
import com.atm.model.Receipt;
import com.atm.model.Session;
import com.atm.model.Transaction;
import com.atm.service.ATMService;
import com.atm.service.AccountService;
import com.atm.service.AuditService;
import com.atm.service.AuthService;
import com.atm.service.BalanceInquiryService;
import com.atm.service.CardService;
import com.atm.service.CustomerService;
import com.atm.service.DepositService;
import com.atm.service.NotificationService;
import com.atm.service.TransactionService;
import com.atm.service.TransferService;
import com.atm.service.WithdrawalService;
import com.atm.util.PinHasher;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final AuditService audit = new AuditService();
    private static final NotificationService notify = new NotificationService();
    private static final CustomerService customerService = new CustomerService();
    private static final AccountService accountService = new AccountService();
    private static final CardService cardService = new CardService(audit);
    private static final TransactionService txnService = new TransactionService();
    private static final AuthService authService = new AuthService(cardService, accountService, audit);
    private static final ATMService atmService = new ATMService();

    private static final WithdrawalService withdrawalService =
            new WithdrawalService(authService, cardService, accountService, txnService, notify, customerService, audit);
    private static final DepositService depositService =
            new DepositService(authService, accountService, txnService, notify, customerService, audit);
    private static final TransferService transferService =
            new TransferService(authService, accountService, txnService, notify, customerService, audit);
    private static final BalanceInquiryService balanceService =
            new BalanceInquiryService(authService, accountService, txnService, audit);

    /** ATM instance currently in use (in real life there are many). */
    private static ATM currentATM;
    /** Card currently inserted (after successful login). */
    private static String currentCardNumber;

    public static void main(String[] args) {
        printBanner();
        bootstrap();
        runHappyPath();

        boolean run = true;
        while (run) {
            printMenu();
            int c = readInt("Choose: ");
            System.out.println();
            try {
                switch (c) {
                    case 1: insertCard();        break;
                    case 2: checkBalance();      break;
                    case 3: withdraw();          break;
                    case 4: deposit();           break;
                    case 5: transfer();          break;
                    case 6: miniStatement();     break;
                    case 7: changePin();         break;
                    case 8: ejectCard();         break;
                    case 9: refillAtm();         break;
                    case 10: blockCard();        break;
                    case 11: showAudit();        break;
                    case 12: showAtmStatus();    break;
                    case 13: run = false;        break;
                    default: System.out.println("Invalid.");
                }
            } catch (ATMException ex) {
                System.out.println("BUSINESS ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("UNEXPECTED: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    private static void bootstrap() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Bootstrap demo data");
        System.out.println("=========================================================");

        // ATM with cash
        ATM atm = atmService.addATM(new ATM("ATM01", "MG Road, Bangalore"));
        atm.refill(Denomination.RS_2000, 50);
        atm.refill(Denomination.RS_500,  100);
        atm.refill(Denomination.RS_200,  50);
        atm.refill(Denomination.RS_100,  200);
        currentATM = atm;

        // 2 customers + accounts + cards
        Customer alice = customerService.register("Alice",   "9000000001", "alice@test.com");
        Customer bob   = customerService.register("Bob",     "9000000002", "bob@test.com");

        Account aliceAcc = accountService.open(alice.getId(), AccountType.SAVINGS, 25000);
        Account bobAcc   = accountService.open(bob.getId(),   AccountType.CURRENT, 50000);

        ATMCard aliceCard = cardService.issue(aliceAcc.getAccountNumber(), "1234", 2);
        ATMCard bobCard   = cardService.issue(bobAcc.getAccountNumber(),   "9876", 2);

        System.out.println("  ATM01 with Rs." + atm.totalCash() + " in cash");
        System.out.println("  Alice account: " + aliceAcc.getAccountNumber() + " card=" + aliceCard.getCardNumber() + " PIN=1234");
        System.out.println("  Bob   account: " + bobAcc.getAccountNumber()   + " card=" + bobCard.getCardNumber()   + " PIN=9876");
        System.out.println();
    }

    private static void runHappyPath() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path demo");
        System.out.println("=========================================================");
        try {
            String aliceCard = cardService.getAll().stream()
                    .filter(c -> accountService.get(c.getAccountNumber()).getCustomerId().equals("C1001"))
                    .findFirst().get().getCardNumber();
            Session s = authService.login(aliceCard, "1234");
            System.out.println("  Login success: " + s.getId());
            currentCardNumber = aliceCard;

            System.out.println("  Balance: Rs." + balanceService.inquire(aliceCard));

            Receipt r = withdrawalService.withdraw(aliceCard, currentATM, 5000);
            System.out.println(r.print());

            Receipt r2 = depositService.deposit(aliceCard, currentATM, 2000);
            System.out.println(r2.print());

            // Bob's account number
            String bobAcc = accountService.getAll().stream()
                    .filter(a -> a.getCustomerId().equals("C1002"))
                    .findFirst().get().getAccountNumber();
            Receipt r3 = transferService.transfer(aliceCard, currentATM, bobAcc, 3000);
            System.out.println(r3.print());

            authService.logout(aliceCard);
            currentCardNumber = null;
            System.out.println("  Card ejected (logged out).");
        } catch (Exception ex) {
            System.out.println("  Happy-path failed: " + ex.getMessage());
        }
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#             JHires ATM Management System               #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        if (currentCardNumber == null) {
            System.out.println(" 1. Insert card (login with PIN)");
        } else {
            System.out.println(" Card inserted: " + PinHasher.maskCardNumber(currentCardNumber));
        }
        System.out.println(" 2. Check balance");
        System.out.println(" 3. Withdraw cash");
        System.out.println(" 4. Deposit cash");
        System.out.println(" 5. Transfer");
        System.out.println(" 6. Mini statement");
        System.out.println(" 7. Change PIN");
        System.out.println(" 8. Eject card (logout)");
        System.out.println("---- Admin ----");
        System.out.println(" 9. Refill ATM cash");
        System.out.println("10. Block card");
        System.out.println("11. Show audit log");
        System.out.println("12. Show ATM status");
        System.out.println("13. Exit");
        System.out.println("==============================================");
    }

    private static void insertCard() {
        String card = readString("Card number : ");
        String pin = readString("PIN         : ");
        Session s = authService.login(card, pin);
        currentCardNumber = card;
        System.out.println("Login OK. Session " + s.getId());
    }

    private static void checkBalance() {
        ensureLoggedIn();
        System.out.println("Balance: Rs." + balanceService.inquire(currentCardNumber));
    }

    private static void withdraw() {
        ensureLoggedIn();
        int amt = readInt("Amount (multiple of 100): ");
        Receipt r = withdrawalService.withdraw(currentCardNumber, currentATM, amt);
        System.out.println(r.print());
    }

    private static void deposit() {
        ensureLoggedIn();
        int amt = readInt("Amount: ");
        Receipt r = depositService.deposit(currentCardNumber, currentATM, amt);
        System.out.println(r.print());
    }

    private static void transfer() {
        ensureLoggedIn();
        String to = readString("To account: ");
        int amt = readInt("Amount: ");
        Receipt r = transferService.transfer(currentCardNumber, currentATM, to, amt);
        System.out.println(r.print());
    }

    private static void miniStatement() {
        ensureLoggedIn();
        Session s = authService.checkAlive(currentCardNumber);
        List<Transaction> list = txnService.miniStatement(s.getAccountNumber(), 5);
        System.out.println("--- Mini Statement (last " + list.size() + ") ---");
        for (Transaction t : list) System.out.println("  " + t);
    }

    private static void changePin() {
        ensureLoggedIn();
        String oldPin = readString("Old PIN: ");
        String newPin = readString("New PIN (4 or 6 digits): ");
        ATMCard card = cardService.get(currentCardNumber);
        card.changePin(oldPin, newPin);
        System.out.println("PIN changed.");
    }

    private static void ejectCard() {
        if (currentCardNumber != null) {
            authService.logout(currentCardNumber);
            currentCardNumber = null;
        }
        System.out.println("Card ejected.");
    }

    private static void refillAtm() {
        System.out.println("Denominations: 1.RS_2000 2.RS_500 3.RS_200 4.RS_100");
        int d = readInt("Choose : ");
        Denomination den = d == 1 ? Denomination.RS_2000
                : d == 2 ? Denomination.RS_500
                : d == 3 ? Denomination.RS_200
                : Denomination.RS_100;
        int notes = readInt("Notes : ");
        currentATM.refill(den, notes);
        System.out.println("Refilled. New total Rs." + currentATM.totalCash());
    }

    private static void blockCard() {
        String c = readString("Card to block : ");
        cardService.block(c, "admin");
        System.out.println("Blocked.");
    }

    private static void showAudit() {
        audit.getLogs().forEach(System.out::println);
    }

    private static void showAtmStatus() {
        System.out.println(currentATM);
        if (currentATM.isLowOnCash()) System.out.println("  LOW CASH ALERT");
    }

    private static void ensureLoggedIn() {
        if (currentCardNumber == null) {
            throw new ATMException("Insert card first (option 1).");
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException ex) { System.out.println("  Invalid number."); }
        }
    }
}
