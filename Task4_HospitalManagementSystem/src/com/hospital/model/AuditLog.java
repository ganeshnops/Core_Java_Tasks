package com.hospital.model;

import java.time.LocalDateTime;

/** Immutable audit record. */
public class AuditLog {

    private final String user;
    private final String action;
    private final String resource;
    private final LocalDateTime timestamp;

    public AuditLog(String user, String action, String resource) {
        this.user = user;
        this.action = action;
        this.resource = resource;
        this.timestamp = LocalDateTime.now();
    }

    public String getUser()     { return user; }
    public String getAction()   { return action; }
    public String getResource() { return resource; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", timestamp, user, action, resource);
    }
}
