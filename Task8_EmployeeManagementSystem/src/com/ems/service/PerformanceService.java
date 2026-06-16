package com.ems.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.exception.UnauthorizedException;
import com.ems.model.Employee;
import com.ems.model.PerformanceReview;

public class PerformanceService {

    private final AtomicLong seq = new AtomicLong(80000);
    private final Map<String, PerformanceReview> reviews = new ConcurrentHashMap<>();

    private final EmployeeService employeeService;
    private final NotificationService notify;

    public PerformanceService(EmployeeService es, NotificationService notify) {
        this.employeeService = es;
        this.notify = notify;
    }

    /** Rule P4 (Performance): Manager can review ONLY their direct reports. */
    public PerformanceReview submitReview(String reviewerId, String employeeId,
                                          String period, int rating, String comments) {
        Employee emp = employeeService.get(employeeId);
        employeeService.get(reviewerId);
        if (!reviewerId.equals(emp.getManagerId())) {
            throw new UnauthorizedException("Only the reporting manager (" + emp.getManagerId()
                    + ") can review " + employeeId);
        }
        String id = "PR-" + seq.incrementAndGet();
        PerformanceReview r = new PerformanceReview(id, employeeId, reviewerId, period, rating, comments);
        reviews.put(id, r);
        notify.notify(employeeId, "Performance review " + id + " submitted by " + reviewerId
                + ", rating=" + rating + "/5");
        return r;
    }

    public List<PerformanceReview> byEmployee(String employeeId) {
        List<PerformanceReview> out = new ArrayList<>();
        for (PerformanceReview r : reviews.values()) if (r.getEmployeeId().equals(employeeId)) out.add(r);
        return out;
    }

    public Collection<PerformanceReview> getAll() { return Collections.unmodifiableCollection(reviews.values()); }
}
