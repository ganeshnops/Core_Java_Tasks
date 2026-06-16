package com.ems.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Department {

    private final String id;
    private final String name;
    private volatile String managerId;
    private final List<TransferLog> transferHistory = new CopyOnWriteArrayList<>();

    public Department(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getManagerId()    { return managerId; }
    public void setManagerId(String m) { this.managerId = m; }

    public List<TransferLog> getTransferHistory() {
        return Collections.unmodifiableList(new ArrayList<>(transferHistory));
    }
    public void addTransferLog(String employeeId, String fromDept) {
        transferHistory.add(new TransferLog(employeeId, fromDept, id));
    }

    public static final class TransferLog {
        public final String employeeId;
        public final String fromDept;
        public final String toDept;
        public final LocalDateTime time = LocalDateTime.now();
        public TransferLog(String employeeId, String fromDept, String toDept) {
            this.employeeId = employeeId; this.fromDept = fromDept; this.toDept = toDept;
        }
        @Override public String toString() {
            return time + " | " + employeeId + " : " + fromDept + " -> " + toDept;
        }
    }

    @Override
    public String toString() {
        return String.format("%s | %s | manager=%s", id, name, managerId == null ? "-" : managerId);
    }
}
