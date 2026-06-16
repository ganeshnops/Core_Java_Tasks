package com.atm.service;

import com.atm.enums.TransactionType;
import com.atm.exception.ATMException;
import com.atm.model.ATM;
import com.atm.model.Account;
import com.atm.model.Receipt;
import com.atm.model.Session;
import com.atm.model.Transaction;

public class DepositService {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService txnService;
    private final NotificationService notify;
    private final CustomerService customerService;
    private final AuditService audit;

    public DepositService(AuthService authService, AccountService accountService,
                          TransactionService txnService, NotificationService notify,
                          CustomerService customerService, AuditService audit) {
        this.authService = authService;
        this.accountService = accountService;
        this.txnService = txnService;
        this.notify = notify;
        this.customerService = customerService;
        this.audit = audit;
    }

    public Receipt deposit(String cardNumber, ATM atm, double amount) {
        Session s = authService.checkAlive(cardNumber);
        if (amount <= 0) throw new ATMException("Amount must be positive.");

        Account account = accountService.get(s.getAccountNumber());
        Transaction txn = txnService.newTransaction(
                new Transaction(TransactionType.DEPOSIT, account.getAccountNumber(), null, amount));
        try {
            account.credit(amount, txn.getId());
            txn.markSuccess();
        } catch (RuntimeException ex) {
            txn.markFailed(ex.getMessage());
            audit.log(account.getCustomerId(), "DEPOSIT_FAIL", account.getAccountNumber(), false);
            throw ex;
        }
        audit.log(account.getCustomerId(), "DEPOSIT_SUCCESS", account.getAccountNumber(), true);
        notify.sms(account.getCustomerId(),
                customerService.get(account.getCustomerId()).getMobile(),
                "Rs." + amount + " deposited to " + account.getAccountNumber()
                        + ". Balance: Rs." + account.getBalance());
        return new Receipt(txn, account.getBalance(), atm.getId());
    }
}
