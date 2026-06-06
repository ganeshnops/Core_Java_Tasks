package com.bank.model;

public class SavingsAccount extends Account {

    public SavingsAccount(String accountNumber, String accountHolder, double initialBalance) {
        super(accountNumber, accountHolder, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }
}
