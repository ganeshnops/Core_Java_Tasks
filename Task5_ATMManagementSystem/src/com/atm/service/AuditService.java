package com.atm.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.atm.model.AuditLog;

public class AuditService {

    private final List<AuditLog> logs = new CopyOnWriteArrayList<>();

    public void log(String actor, String action, String resource, boolean success) {
        logs.add(new AuditLog(actor, action, resource, success));
    }

    public List<AuditLog> getLogs() { return Collections.unmodifiableList(logs); }
}
