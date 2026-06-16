package com.atm.service;

import java.util.concurrent.locks.ReentrantLock;

import com.atm.enums.TransactionType;
import com.atm.exception.ATMException;
import com.atm.model.ATM;
import com.atm.model.Account;
import com.atm.model.Receipt;
import com.atm.model.Session;
import com.atm.model.Transaction;

/**
 * Fund transfer with deadlock prevention (Task 1 pattern).
 * Both threads always lock the SMALLER account number first.
 */
public class TransferService {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService txnService;
    private final NotificationService notify;
    private final CustomerService customerService;
    private final AuditService audit;

    public TransferService(AuthService authService, AccountService accountService,
                           TransactionService txnService, NotificationService notify,
                           CustomerService customerService, AuditService audit) {
        this.authService = authService;
        this.accountService = accountService;
        this.txnService = txnService;
        this.notify = notify;
        this.customerService = customerService;
        this.audit = audit;
    }

    public Receipt transfer(String cardNumber, ATM atm, String toAccountNumber, double amount) {
        Session s = authService.checkAlive(cardNumber);
        if (amount <= 0) throw new ATMException("Amount must be positive.");

        Account from = accountService.get(s.getAccountNumber());
        Account to   = accountService.get(toAccountNumber);
        from.ensureActive();
        to.ensureActive();
        if (from.getAccountNumber().equals(to.getAccountNumber())) {
            throw new ATMException("Source and destination cannot be the same.");
        }

        Transaction txn = txnService.newTransaction(new Transaction(
                TransactionType.TRANSFER_OUT, from.getAccountNumber(), to.getAccountNumber(), amount));

        // Deadlock prevention - lock smaller account number first
        Account first  = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account second = first == from ? to : from;
        ReentrantLock l1 = first.getLock();
        ReentrantLock l2 = second.getLock();
        l1.lock();
        try {
            l2.lock();
            try {
                from.debitUnderLock(amount, txn.getId());
                to.creditUnderLock(amount, txn.getId());
                txn.markSuccess();
            } finally { l2.unlock(); }
        } finally { l1.unlock(); }

        audit.log(from.getCustomerId(), "TRANSFER_SUCCESS",
                from.getAccountNumber() + "->" + to.getAccountNumber(), true);
        notify.sms(from.getCustomerId(),
                customerService.get(from.getCustomerId()).getMobile(),
                "Transfer of Rs." + amount + " from " + from.getAccountNumber()
                        + " to " + to.getAccountNumber() + ". Balance: Rs." + from.getBalance());
        return new Receipt(txn, from.getBalance(), atm.getId());
    }
}
