package com.ems.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.exception.NotFoundException;
import com.ems.model.Employee;
import com.ems.model.Payslip;
import com.ems.model.Salary;

/**
 * Payroll processing.
 *  - Monthly batch generation
 *  - Locked after processing (cannot modify)
 *  - Auto-calculation of PF (12% basic), tax (10% of taxable income)
 */
public class PayrollService {

    private final AtomicLong seq = new AtomicLong(900000);
    private final Map<String, Salary> salaries = new ConcurrentHashMap<>();
    private final Map<String, Payslip> payslips = new ConcurrentHashMap<>();   // by id
    private final Map<String, Map<YearMonth, String>> employeeMonthIndex = new ConcurrentHashMap<>();

    private final EmployeeService employeeService;
    private final NotificationService notify;

    public PayrollService(EmployeeService es, NotificationService notify) {
        this.employeeService = es;
        this.notify = notify;
    }

    public void setSalary(Salary s) { salaries.put(s.getEmployeeId(), s); }
    public Salary getSalary(String employeeId) {
        Salary s = salaries.get(employeeId);
        if (s == null) throw new NotFoundException("No salary set for " + employeeId);
        return s;
    }

    /** Generate payslip for one employee. */
    public Payslip generatePayslip(String employeeId, YearMonth period, double bonus) {
        Employee e = employeeService.get(employeeId);
        Salary s = getSalary(employeeId);
        Map<YearMonth, String> idx = employeeMonthIndex.computeIfAbsent(employeeId, k -> new ConcurrentHashMap<>());
        if (idx.containsKey(period)) {
            return payslips.get(idx.get(period));   // already generated - return existing
        }
        String id = "PS-" + seq.incrementAndGet();
        Payslip p = new Payslip(id, employeeId, period, s.getBasic(), s.getHra(), s.getAllowances(), bonus);
        payslips.put(id, p);
        idx.put(period, id);
        notify.notify(employeeId, "Payslip " + id + " generated for " + period + ", net=Rs." + p.getNetSalary());
        return p;
    }

    /** Process payroll for ALL active employees in given month. */
    public List<Payslip> generateMonthly(YearMonth period) {
        List<Payslip> out = new ArrayList<>();
        for (Employee e : employeeService.getAll()) {
            if (!e.isActive()) continue;
            if (salaries.get(e.getId()) == null) continue;
            out.add(generatePayslip(e.getId(), period, 0));
        }
        return out;
    }

    public void process(String payslipId) {
        Payslip p = payslips.get(payslipId);
        if (p == null) throw new NotFoundException("Payslip not found: " + payslipId);
        p.process();
    }

    public Collection<Payslip> getAll() { return Collections.unmodifiableCollection(payslips.values()); }
}
