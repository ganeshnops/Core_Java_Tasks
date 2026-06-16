package com.ems.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ems.enums.ProjectStatus;
import com.ems.exception.EMSException;

public class Project {

    private final String id;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private volatile ProjectStatus status;
    private final Set<String> assignedEmployees = ConcurrentHashMap.newKeySet();

    public Project(String id, String name, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new EMSException("Project start must be before end.");
        }
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ProjectStatus.ACTIVE;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public LocalDate getStartDate()   { return startDate; }
    public LocalDate getEndDate()     { return endDate; }
    public ProjectStatus getStatus()  { return status; }
    public Set<String> getAssignedEmployees() {
        return Collections.unmodifiableSet(assignedEmployees);
    }

    public void setStatus(ProjectStatus s) { this.status = s; }

    /** Assign employee. Rule P6: duplicate assignment rejected. */
    public boolean assign(String employeeId) {
        return assignedEmployees.add(employeeId);
    }

    public void unassign(String employeeId) { assignedEmployees.remove(employeeId); }

    @Override
    public String toString() {
        return String.format("%s | %s | %s -> %s | %s | %d employees",
                id, name, startDate, endDate, status, assignedEmployees.size());
    }
}
