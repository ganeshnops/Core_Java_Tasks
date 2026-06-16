package com.atm.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.atm.enums.AccountStatus;
import com.atm.enums.CardStatus;
import com.atm.exception.ATMException;
import com.atm.exception.SessionExpiredException;
import com.atm.model.ATMCard;
import com.atm.model.Account;
import com.atm.model.Session;
import com.atm.util.PinHasher;

/**
 * Authentication + session management.
 *  - Rule Security 1: ATM session requires card + PIN.
 *  - Rule Security 2: session expires after inactivity.
 *  - Rule Security 3: sensitive info (card number) masked.
 *  - Rule Security 5: unauthorized attempts logged.
 */
public class AuthService {

    /** Session idle timeout - 2 minutes (real ATM ~30 sec to 2 min). */
    public static final long SESSION_IDLE_MS = TimeUnit.MINUTES.toMillis(2);

    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();   // cardNumber -> session

    private final CardService cardService;
    private final AccountService accountService;
    private final AuditService auditService;

    public AuthService(CardService cardService, AccountService accountService, AuditService auditService) {
        this.cardService = cardService;
        this.accountService = accountService;
        this.auditService = auditService;
    }

    /** Authenticate card + PIN, start a session. */
    public Session login(String cardNumber, String pin) {
        ATMCard card;
        try {
            card = cardService.get(cardNumber);
        } catch (ATMException ex) {
            auditService.log("UNKNOWN", "LOGIN_ATTEMPT_UNKNOWN_CARD",
                    PinHasher.maskCardNumber(cardNumber), false);
            throw ex;
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            auditService.log("CARD-" + cardNumber.substring(cardNumber.length() - 4),
                    "LOGIN_BLOCKED_CARD", PinHasher.maskCardNumber(cardNumber), false);
            throw new ATMException("Card BLOCKED.");
        }
        try {
            card.verifyPin(pin);
        } catch (RuntimeException ex) {
            auditService.log("CARD-" + cardNumber.substring(cardNumber.length() - 4),
                    "LOGIN_FAIL", PinHasher.maskCardNumber(cardNumber), false);
            throw ex;
        }
        Account a = accountService.get(card.getAccountNumber());
        if (a.getStatus() != AccountStatus.ACTIVE) {
            throw new ATMException("Account is " + a.getStatus());
        }
        Session session = new Session(cardNumber, a.getAccountNumber());
        activeSessions.put(cardNumber, session);
        auditService.log(a.getCustomerId(), "LOGIN_SUCCESS",
                PinHasher.maskCardNumber(cardNumber), true);
        return session;
    }

    /** Validate session is alive (refresh activity timestamp on success). */
    public Session checkAlive(String cardNumber) {
        Session s = activeSessions.get(cardNumber);
        if (s == null) throw new SessionExpiredException("No active session.");
        if (s.isExpired(SESSION_IDLE_MS)) {
            activeSessions.remove(cardNumber);
            throw new SessionExpiredException("Session expired (inactive too long).");
        }
        s.touch();
        return s;
    }

    public void logout(String cardNumber) {
        activeSessions.remove(cardNumber);
    }
}
