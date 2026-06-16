package com.atm.service;

import com.atm.enums.TransactionType;
import com.atm.model.Account;
import com.atm.model.Session;
import com.atm.model.Transaction;

public class BalanceInquiryService {

    private final AuthService authService;
    private final AccountService accountService;
    private final TransactionService txnService;
    private final AuditService audit;

    public BalanceInquiryService(AuthService authService, AccountService accountService,
                                 TransactionService txnService, AuditService audit) {
        this.authService = authService;
        this.accountService = accountService;
        this.txnService = txnService;
        this.audit = audit;
    }

    public double inquire(String cardNumber) {
        Session s = authService.checkAlive(cardNumber);
        Account a = accountService.get(s.getAccountNumber());
        Transaction t = new Transaction(TransactionType.BALANCE_INQUIRY,
                a.getAccountNumber(), null, 0);
        t.markSuccess();
        txnService.newTransaction(t);
        audit.log(a.getCustomerId(), "BALANCE_INQUIRY", a.getAccountNumber(), true);
        return a.getBalance();
    }
}
