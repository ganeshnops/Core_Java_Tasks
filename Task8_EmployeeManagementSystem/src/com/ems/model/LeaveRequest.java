package com.ems.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.ems.enums.LeaveStatus;
import com.ems.enums.LeaveType;
import com.ems.exception.LeaveException;

public class LeaveRequest {

    private final String id;
    private final String employeeId;
    private final LeaveType type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;
    private volatile LeaveStatus status;
    private volatile String approvedBy;
    private final LocalDateTime requestedAt;
    private volatile LocalDateTime decidedAt;
    private final ReentrantLock lock = new ReentrantLock();

    public LeaveRequest(String id, String employeeId, LeaveType type,
                        LocalDate startDate, LocalDate endDate, String reason) {
        if (startDate.isAfter(endDate)) {
            throw new LeaveException("Start date cannot be after end date.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new LeaveException("Cannot apply for past dates.");
        }
        this.id = id;
        this.employeeId = employeeId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.requestedAt = LocalDateTime.now();
        this.status = LeaveStatus.PENDING;
    }

    public String getId()                  { return id; }
    public String getEmployeeId()          { return employeeId; }
    public LeaveType getType()             { return type; }
    public LocalDate getStartDate()        { return startDate; }
    public LocalDate getEndDate()          { return endDate; }
    public String getReason()              { return reason; }
    public LeaveStatus getStatus()         { return status; }
    public String getApprovedBy()          { return approvedBy; }
    public LocalDateTime getRequestedAt()  { return requestedAt; }
    public LocalDateTime getDecidedAt()    { return decidedAt; }

    public void setStatus(LeaveStatus s)   { this.status = s; }
    public void setApprovedBy(String a)    { this.approvedBy = a; }
    public void setDecidedAt(LocalDateTime t) { this.decidedAt = t; }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean overlapsWith(LocalDate otherStart, LocalDate otherEnd) {
        return !startDate.isAfter(otherEnd) && !otherStart.isAfter(endDate);
    }

    public ReentrantLock getLock() { return lock; }

    @Override
    public String toString() {
        return String.format("%s | emp=%s | %s | %s -> %s (%d days) | %s | by=%s",
                id, employeeId, type, startDate, endDate, getDays(), status,
                approvedBy == null ? "-" : approvedBy);
    }
}
