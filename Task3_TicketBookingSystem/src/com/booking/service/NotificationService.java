package com.booking.service;

public class NotificationService {

    public void notify(String customerId, String message) {
        System.out.println("  [notify " + customerId + "] " + message);
    }
}
