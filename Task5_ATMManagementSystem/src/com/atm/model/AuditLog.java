package com.atm.model;

import java.time.LocalDateTime;

public class AuditLog {

    private final String actor;          // customerId or "admin"
    private final String action;
    private final String resource;
    private final boolean success;
    private final LocalDateTime timestamp;

    public AuditLog(String actor, String action, String resource, boolean success) {
        this.actor = actor;
        this.action = action;
        this.resource = resource;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    public String getActor()    { return actor; }
    public String getAction()   { return action; }
    public String getResource() { return resource; }
    public boolean isSuccess()  { return success; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s",
                timestamp, actor, action, resource, success ? "SUCCESS" : "FAILED");
    }
}
