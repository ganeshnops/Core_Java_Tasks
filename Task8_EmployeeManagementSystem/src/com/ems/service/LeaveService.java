package com.ems.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.enums.LeaveStatus;
import com.ems.enums.LeaveType;
import com.ems.exception.EMSException;
import com.ems.exception.LeaveException;
import com.ems.exception.NotFoundException;
import com.ems.exception.UnauthorizedException;
import com.ems.model.Employee;
import com.ems.model.LeaveBalance;
import com.ems.model.LeaveRequest;

public class LeaveService {

    private final AtomicLong seq = new AtomicLong(50000);
    private final Map<String, LeaveRequest> requests = new ConcurrentHashMap<>();
    private final Map<String, LeaveBalance> balances = new ConcurrentHashMap<>();

    private final EmployeeService employeeService;
    private final NotificationService notify;

    public LeaveService(EmployeeService es, NotificationService notify) {
        this.employeeService = es;
        this.notify = notify;
    }

    public LeaveBalance getBalance(String employeeId) {
        return balances.computeIfAbsent(employeeId, LeaveBalance::new);
    }

    public LeaveRequest apply(String employeeId, LeaveType type,
                              LocalDate startDate, LocalDate endDate, String reason) {
        Employee e = employeeService.get(employeeId);
        if (!e.isActive()) throw new LeaveException("Only ACTIVE employees can apply for leave.");

        // Overlap check
        for (LeaveRequest existing : requests.values()) {
            if (!existing.getEmployeeId().equals(employeeId)) continue;
            if (existing.getStatus() == LeaveStatus.CANCELLED
                    || existing.getStatus() == LeaveStatus.REJECTED) continue;
            if (existing.overlapsWith(startDate, endDate)) {
                throw new LeaveException("Overlapping leave request exists: " + existing.getId());
            }
        }
        String id = "LR-" + seq.incrementAndGet();
        LeaveRequest req = new LeaveRequest(id, employeeId, type, startDate, endDate, reason);
        requests.put(id, req);

        // Notify manager
        if (e.getManagerId() != null) {
            notify.notify(e.getManagerId(), "Leave request " + id + " from " + employeeId + " for approval");
        }
        return req;
    }

    public void approve(String requestId, String approverId) {
        LeaveRequest r = requests.get(requestId);
        if (r == null) throw new NotFoundException("Leave request not found: " + requestId);
        if (r.getEmployeeId().equals(approverId)) {
            throw new UnauthorizedException("Cannot approve own leave.");
        }
        // Approver must be employee's manager
        Employee emp = employeeService.get(r.getEmployeeId());
        if (!approverId.equals(emp.getManagerId())) {
            throw new UnauthorizedException("Only the reporting manager can approve.");
        }
        r.getLock().lock();
        try {
            if (r.getStatus() != LeaveStatus.PENDING) {
                throw new EMSException("Leave request already decided.");
            }
            // Deduct from balance
            double days = r.getDays();
            LeaveBalance b = getBalance(r.getEmployeeId());
            if (!b.deduct(r.getType(), days)) {
                throw new LeaveException("Insufficient " + r.getType() + " leave balance.");
            }
            r.setStatus(LeaveStatus.APPROVED);
            r.setApprovedBy(approverId);
            r.setDecidedAt(LocalDateTime.now());
            notify.notify(r.getEmployeeId(), "Leave " + requestId + " APPROVED by " + approverId);
        } finally { r.getLock().unlock(); }
    }

    public void reject(String requestId, String approverId, String reason) {
        LeaveRequest r = requests.get(requestId);
        if (r == null) throw new NotFoundException("Leave request not found: " + requestId);
        Employee emp = employeeService.get(r.getEmployeeId());
        if (!approverId.equals(emp.getManagerId())) {
            throw new UnauthorizedException("Only the reporting manager can reject.");
        }
        r.getLock().lock();
        try {
            if (r.getStatus() != LeaveStatus.PENDING) throw new EMSException("Already decided.");
            r.setStatus(LeaveStatus.REJECTED);
            r.setApprovedBy(approverId);
            r.setDecidedAt(LocalDateTime.now());
            notify.notify(r.getEmployeeId(), "Leave " + requestId + " REJECTED. Reason: " + reason);
        } finally { r.getLock().unlock(); }
    }

    public void cancel(String requestId) {
        LeaveRequest r = requests.get(requestId);
        if (r == null) throw new NotFoundException("Leave request not found: " + requestId);
        r.getLock().lock();
        try {
            if (r.getStatus() == LeaveStatus.APPROVED) {
                // restore balance
                getBalance(r.getEmployeeId()).restore(r.getType(), r.getDays());
            }
            r.setStatus(LeaveStatus.CANCELLED);
        } finally { r.getLock().unlock(); }
    }

    public Collection<LeaveRequest> getAll() { return Collections.unmodifiableCollection(requests.values()); }

    public List<LeaveRequest> byEmployee(String employeeId) {
        List<LeaveRequest> out = new ArrayList<>();
        for (LeaveRequest r : requests.values()) if (r.getEmployeeId().equals(employeeId)) out.add(r);
        return out;
    }
}
