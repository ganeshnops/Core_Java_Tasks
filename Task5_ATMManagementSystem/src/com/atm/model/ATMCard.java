package com.atm.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import com.atm.enums.CardStatus;
import com.atm.exception.CardException;
import com.atm.exception.InvalidPinException;
import com.atm.util.PinHasher;

/**
 * ATM card.
 *  - Rule Card 1: unique card number
 *  - Rule Card 2,3: expiry date check
 *  - Rule Card 4: linked to one account
 *  - Rule Card 5,6: status ACTIVE/BLOCKED/EXPIRED
 *  - PIN rules: 4-or-6 digit, hashed, 3 attempts -> block.
 */
public class ATMCard {

    public static final int MAX_FAILED_ATTEMPTS = 3;

    private final String cardNumber;
    private final String accountNumber;
    private final LocalDate expiry;
    private volatile String pinHash;
    private volatile CardStatus status;
    private final AtomicInteger failedAttempts = new AtomicInteger(0);

    public ATMCard(String cardNumber, String accountNumber, String pin, LocalDate expiry) {
        if (pin == null || (pin.length() != 4 && pin.length() != 6) || !pin.chars().allMatch(Character::isDigit)) {
            throw new CardException("PIN must be exactly 4 or 6 digits.");
        }
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
        this.pinHash = PinHasher.hash(pin);
        this.expiry = expiry;
        this.status = CardStatus.ACTIVE;
    }

    public String getCardNumber()     { return cardNumber; }
    public String getAccountNumber()  { return accountNumber; }
    public LocalDate getExpiry()      { return expiry; }
    public CardStatus getStatus()     { return status; }
    public int getFailedAttempts()    { return failedAttempts.get(); }

    public void setStatus(CardStatus s) { this.status = s; }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiry);
    }

    /** Verify the PIN. Increments failedAttempts on failure. Blocks card after MAX. */
    public boolean verifyPin(String pin) {
        if (status == CardStatus.BLOCKED)        throw new CardException("Card is BLOCKED.");
        if (isExpired()) {
            status = CardStatus.EXPIRED;
            throw new CardException("Card is EXPIRED.");
        }
        if (PinHasher.matches(pin, pinHash)) {
            failedAttempts.set(0);
            return true;
        }
        int attempts = failedAttempts.incrementAndGet();
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            status = CardStatus.BLOCKED;
            throw new InvalidPinException("Wrong PIN. Card BLOCKED after " + MAX_FAILED_ATTEMPTS + " failed attempts.");
        }
        throw new InvalidPinException("Wrong PIN. " + (MAX_FAILED_ATTEMPTS - attempts) + " attempts remaining.");
    }

    /** Change PIN - must verify old PIN; new PIN must not equal old. */
    public void changePin(String oldPin, String newPin) {
        if (!PinHasher.matches(oldPin, pinHash)) {
            throw new InvalidPinException("Old PIN incorrect.");
        }
        if (PinHasher.matches(newPin, pinHash)) {
            throw new InvalidPinException("New PIN cannot be the same as old PIN.");
        }
        if (newPin == null || (newPin.length() != 4 && newPin.length() != 6) || !newPin.chars().allMatch(Character::isDigit)) {
            throw new CardException("PIN must be 4 or 6 digits.");
        }
        this.pinHash = PinHasher.hash(newPin);
    }

    @Override
    public String toString() {
        return String.format("%s | account=%s | exp=%s | %s | failedAttempts=%d",
                PinHasher.maskCardNumber(cardNumber), accountNumber, expiry, status, failedAttempts.get());
    }
}
