package com.bank.model;

public class CurrentAccount extends Account {

    public CurrentAccount(String accountNumber, String accountHolder, double initialBalance) {
        super(accountNumber, accountHolder, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }
}
