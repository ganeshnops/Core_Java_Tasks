package com.atm.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.atm.enums.Denomination;
import com.atm.enums.TransactionType;
import com.atm.exception.ATMException;
import com.atm.exception.InsufficientATMCashException;
import com.atm.exception.WithdrawalLimitException;
import com.atm.model.ATM;
import com.atm.model.ATMCard;
import com.atm.model.Account;
import com.atm.model.Receipt;
import com.atm.model.Session;
import com.atm.model.Transaction;

/**
 * Withdrawal flow.
 *  - Rule W1: positive amount
 *  - Rule W2: multiple of 100
 *  - Rule W3: sufficient balance (Account.debit checks)
 *  - Rule W4: daily withdrawal limit
 *  - Rule W5: per-transaction limit
 *  - Rule W6: deduct balance only once (atomic flow)
 *  - Rule W7: cash dispensed ONLY after successful debit
 *
 * Order: validate -> debit account -> dispense from ATM -> log -> notify.
 * If dispense fails AFTER debit, we credit it back (compensating action).
 */
public class WithdrawalService {

    public static final double DAILY_LIMIT = 50_000;
    public static final double PER_TXN_LIMIT = 20_000;
    public static final int    MULTIPLE_OF   = 100;

    /** customerId|date -> total withdrawn today. */
    private final Map<String, Double> dailyTotals = new ConcurrentHashMap<>();

    private final AuthService authService;
    private final CardService cardService;
    private final AccountService accountService;
    private final TransactionService txnService;
    private final NotificationService notify;
    private final CustomerService customerService;
    private final AuditService audit;

    public WithdrawalService(AuthService authService, CardService cardService,
                             AccountService accountService, TransactionService txnService,
                             NotificationService notify, CustomerService customerService,
                             AuditService audit) {
        this.authService = authService;
        this.cardService = cardService;
        this.accountService = accountService;
        this.txnService = txnService;
        this.notify = notify;
        this.customerService = customerService;
        this.audit = audit;
    }

    public Receipt withdraw(String cardNumber, ATM atm, double amount) {
        Session s = authService.checkAlive(cardNumber);
        ATMCard card = cardService.get(cardNumber);
        Account account = accountService.get(s.getAccountNumber());

        // Validations
        if (amount <= 0) throw new ATMException("Amount must be positive.");
        if (amount % MULTIPLE_OF != 0) {
            throw new ATMException("Amount must be a multiple of Rs." + MULTIPLE_OF);
        }
        if (amount > PER_TXN_LIMIT) {
            throw new WithdrawalLimitException("Per-transaction limit Rs." + PER_TXN_LIMIT);
        }
        String dailyKey = account.getCustomerId() + "|" + LocalDate.now();
        double todaySoFar = dailyTotals.getOrDefault(dailyKey, 0.0);
        if (todaySoFar + amount > DAILY_LIMIT) {
            throw new WithdrawalLimitException("Daily limit Rs." + DAILY_LIMIT + " exceeded.");
        }

        Transaction txn = txnService.newTransaction(
                new Transaction(TransactionType.WITHDRAW, account.getAccountNumber(), null, amount));

        // Debit FIRST, then dispense (Rule W7). If dispense fails, credit back.
        try {
            account.debit(amount, txn.getId());
        } catch (RuntimeException ex) {
            txn.markFailed(ex.getMessage());
            audit.log(account.getCustomerId(), "WITHDRAW_DEBIT_FAIL", account.getAccountNumber(), false);
            throw ex;
        }
        Map<Denomination, Integer> notes;
        try {
            notes = atm.dispense((int) amount);
        } catch (InsufficientATMCashException ex) {
            // Compensating credit
            account.credit(amount, txn.getId() + "-REVERSE");
            txn.markFailed("ATM cash insufficient: " + ex.getMessage());
            audit.log(account.getCustomerId(), "WITHDRAW_DISPENSE_FAIL", account.getAccountNumber(), false);
            throw ex;
        }

        txn.markSuccess();
        dailyTotals.merge(dailyKey, amount, Double::sum);
        audit.log(account.getCustomerId(), "WITHDRAW_SUCCESS", account.getAccountNumber(), true);

        String customerId = account.getCustomerId();
        notify.sms(customerId,
                customerService.get(customerId).getMobile(),
                "Rs." + amount + " withdrawn from " + account.getAccountNumber()
                + " at ATM " + atm.getId() + ". Balance: Rs." + account.getBalance());

        if (atm.isLowOnCash()) {
            System.out.println("  [ATM alert] " + atm.getId() + " is low on cash.");
        }
        System.out.println("  Notes dispensed: " + notes);
        return new Receipt(txn, account.getBalance(), atm.getId());
    }
}
