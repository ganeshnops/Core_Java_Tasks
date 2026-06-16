package com.ems.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Same SHA-256 + salt pattern as Task 5 ATM. */
public final class PasswordHasher {

    private static final String SALT = "EMS_Salt_2026";

    private PasswordHasher() {}

    public static String hash(String pwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((SALT + pwd).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String pwd, String storedHash) {
        return hash(pwd).equals(storedHash);
    }
}
