package com.atm.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.atm.enums.CardStatus;
import com.atm.exception.ATMException;
import com.atm.model.ATMCard;

public class CardService {

    private final AtomicLong seq = new AtomicLong(4111_1111_1111_1110L);
    private final Map<String, ATMCard> cards = new ConcurrentHashMap<>();
    private final AuditService audit;

    public CardService(AuditService audit) { this.audit = audit; }

    public ATMCard issue(String accountNumber, String pin, int expiryYears) {
        String cardNumber = String.valueOf(seq.incrementAndGet());
        ATMCard card = new ATMCard(cardNumber, accountNumber, pin,
                LocalDate.now().plusYears(expiryYears));
        cards.put(cardNumber, card);
        audit.log("system", "CARD_ISSUE", cardNumber, true);
        return card;
    }

    public ATMCard get(String cardNumber) {
        ATMCard c = cards.get(cardNumber);
        if (c == null) throw new ATMException("Card not found: " + cardNumber);
        return c;
    }

    public Collection<ATMCard> getAll() { return Collections.unmodifiableCollection(cards.values()); }

    /** Admin operation to block compromised cards. */
    public void block(String cardNumber, String adminId) {
        ATMCard c = get(cardNumber);
        c.setStatus(CardStatus.BLOCKED);
        audit.log(adminId, "CARD_BLOCK", cardNumber, true);
    }

    public void unblock(String cardNumber, String adminId) {
        ATMCard c = get(cardNumber);
        if (c.isExpired()) throw new ATMException("Cannot unblock expired card.");
        c.setStatus(CardStatus.ACTIVE);
        audit.log(adminId, "CARD_UNBLOCK", cardNumber, true);
    }
}
