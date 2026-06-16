package com.atm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PIN hashing utility.
 *  - Rule PIN 2: PIN should be encrypted (we use SHA-256 one-way hash + salt).
 *  - Never store plain PIN. Hash on entry and compare hashes.
 */
public final class PinHasher {

    private static final String SALT = "ATMSecureSalt_v1";

    private PinHasher() {}

    public static String hash(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPin = SALT + pin;
            byte[] digest = md.digest(saltedPin.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String pin, String storedHash) {
        return hash(pin).equals(storedHash);
    }

    /** Mask a card number - shows only last 4 digits. */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() <= 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
