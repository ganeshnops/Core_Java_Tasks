package com.atm.model;

import java.time.format.DateTimeFormatter;

public class Receipt {

    private final Transaction transaction;
    private final double balanceAfter;
    private final String atmId;

    public Receipt(Transaction transaction, double balanceAfter, String atmId) {
        this.transaction = transaction;
        this.balanceAfter = balanceAfter;
        this.atmId = atmId;
    }

    public Transaction getTransaction() { return transaction; }
    public double getBalanceAfter()     { return balanceAfter; }
    public String getAtmId()            { return atmId; }

    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append("===========================================\n");
        sb.append("            ATM RECEIPT\n");
        sb.append("===========================================\n");
        sb.append("ATM ID         : ").append(atmId).append("\n");
        sb.append("Transaction ID : ").append(transaction.getId()).append("\n");
        sb.append("Type           : ").append(transaction.getType()).append("\n");
        sb.append("Account        : ").append(transaction.getFromAccount()).append("\n");
        if (transaction.getToAccount() != null) {
            sb.append("To Account     : ").append(transaction.getToAccount()).append("\n");
        }
        sb.append("Amount         : Rs.").append(String.format("%.2f", transaction.getAmount())).append("\n");
        sb.append("Balance        : Rs.").append(String.format("%.2f", balanceAfter)).append("\n");
        sb.append("Status         : ").append(transaction.getStatus()).append("\n");
        sb.append("Time           : ").append(transaction.getTimestamp()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("===========================================\n");
        return sb.toString();
    }
}
