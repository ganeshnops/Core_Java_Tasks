package com.atm.service;

public class NotificationService {
    public void sms(String customerId, String mobile, String message) {
        System.out.println("  [SMS to " + customerId + " (" + mobile + ")] " + message);
    }
}
