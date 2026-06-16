package com.ems.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ems.enums.AssetStatus;

public class Asset {

    private final String id;
    private final String name;
    private final String category;        // Laptop, Phone, etc.
    private volatile AssetStatus status;
    private volatile String currentEmployeeId;
    private final List<AssignLog> history = new CopyOnWriteArrayList<>();

    public Asset(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.status = AssetStatus.AVAILABLE;
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getCategory()        { return category; }
    public AssetStatus getStatus()     { return status; }
    public String getCurrentEmployeeId() { return currentEmployeeId; }
    public List<AssignLog> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    public void setStatus(AssetStatus s) { this.status = s; }

    public void assignTo(String employeeId) {
        history.add(new AssignLog(employeeId, "ASSIGN"));
        this.currentEmployeeId = employeeId;
        this.status = AssetStatus.ASSIGNED;
    }
    public void returnAsset() {
        history.add(new AssignLog(currentEmployeeId, "RETURN"));
        this.currentEmployeeId = null;
        this.status = AssetStatus.AVAILABLE;
    }
    public void markLost() {
        history.add(new AssignLog(currentEmployeeId, "LOST"));
        this.status = AssetStatus.LOST;
    }

    public static final class AssignLog {
        public final String employeeId;
        public final String action;
        public final LocalDateTime time = LocalDateTime.now();
        public AssignLog(String employeeId, String action) {
            this.employeeId = employeeId; this.action = action;
        }
        @Override public String toString() {
            return time + " | " + employeeId + " | " + action;
        }
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | with=%s",
                id, name, category, status, currentEmployeeId == null ? "-" : currentEmployeeId);
    }
}
