package com.ems.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ems.enums.EmployeeStatus;
import com.ems.exception.EMSException;

public class Employee {

    private final String id;
    private final String name;
    private final String email;
    private final String mobile;
    private final LocalDate dateOfJoining;
    private volatile String departmentId;
    private volatile String designation;
    private volatile String managerId;     // reporting manager
    private volatile EmployeeStatus status;
    private final List<String> managerChangeLog = new CopyOnWriteArrayList<>();

    public Employee(String id, String name, String email, String mobile,
                    LocalDate dateOfJoining, String departmentId, String designation) {
        if (name == null || name.isBlank()) throw new EMSException("Employee name is mandatory.");
        if (dateOfJoining == null)          throw new EMSException("Date of joining is mandatory.");
        if (departmentId == null)           throw new EMSException("Department is mandatory.");
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.dateOfJoining = dateOfJoining;
        this.departmentId = departmentId;
        this.designation = designation;
        this.status = EmployeeStatus.ACTIVE;
    }

    public String getId()                 { return id; }
    public String getName()               { return name; }
    public String getEmail()              { return email; }
    public String getMobile()             { return mobile; }
    public LocalDate getDateOfJoining()   { return dateOfJoining; }
    public String getDepartmentId()       { return departmentId; }
    public String getDesignation()        { return designation; }
    public String getManagerId()          { return managerId; }
    public EmployeeStatus getStatus()     { return status; }
    public List<String> getManagerChangeLog() {
        return Collections.unmodifiableList(new ArrayList<>(managerChangeLog));
    }

    public void setDepartmentId(String d)   { this.departmentId = d; }
    public void setDesignation(String d)    { this.designation = d; }
    public void setStatus(EmployeeStatus s) { this.status = s; }

    public void setManagerId(String newMgr) {
        managerChangeLog.add(LocalDateTime.now() + " | " + (this.managerId == null ? "-" : this.managerId) + " -> " + newMgr);
        this.managerId = newMgr;
    }

    public boolean isActive() { return status == EmployeeStatus.ACTIVE; }
    public boolean canAccessSystem() {
        return status == EmployeeStatus.ACTIVE || status == EmployeeStatus.INACTIVE;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | dept=%s | %s | mgr=%s | %s",
                id, name, email, mobile, departmentId, designation,
                managerId == null ? "-" : managerId, status);
    }
}
