package com.hospital.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hospital.model.AuditLog;

public class AuditService {

    private final List<AuditLog> logs = new CopyOnWriteArrayList<>();

    public void log(String user, String action, String resource) {
        logs.add(new AuditLog(user, action, resource));
    }

    public List<AuditLog> getLogs() { return Collections.unmodifiableList(logs); }
}
