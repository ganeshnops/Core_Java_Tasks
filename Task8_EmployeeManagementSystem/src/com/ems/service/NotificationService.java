package com.ems.service;

public class NotificationService {
    public void notify(String to, String message) {
        System.out.println("  [notify " + to + "] " + message);
    }
}
