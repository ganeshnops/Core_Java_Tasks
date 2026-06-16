package com.ems.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.enums.EmployeeStatus;
import com.ems.exception.CircularReportingException;
import com.ems.exception.DuplicateException;
import com.ems.exception.EMSException;
import com.ems.exception.NotFoundException;
import com.ems.model.Department;
import com.ems.model.Employee;

/**
 * Employee + manager management.
 *  - Unique mobile, email, username
 *  - Status machine
 *  - CIRCULAR REPORTING DETECTION using DFS through reporting graph
 *  - Department transfers logged
 */
public class EmployeeService {

    private final AtomicLong seq = new AtomicLong(1000);
    private final Map<String, Employee> employees = new ConcurrentHashMap<>();
    private final Set<String> usedEmails = ConcurrentHashMap.newKeySet();
    private final Set<String> usedMobiles = ConcurrentHashMap.newKeySet();

    private final DepartmentService departmentService;

    public EmployeeService(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    public Employee register(String name, String email, String mobile,
                             LocalDate dateOfJoining, String departmentId, String designation) {
        if (!usedEmails.add(email))   throw new DuplicateException("Email in use: " + email);
        if (!usedMobiles.add(mobile)) throw new DuplicateException("Mobile in use: " + mobile);
        departmentService.get(departmentId);   // validates exists
        String id = "E" + seq.incrementAndGet();
        Employee e = new Employee(id, name, email, mobile, dateOfJoining, departmentId, designation);
        employees.put(id, e);
        return e;
    }

    public Employee get(String id) {
        Employee e = employees.get(id);
        if (e == null) throw new NotFoundException("Employee not found: " + id);
        return e;
    }

    public Collection<Employee> getAll() {
        return Collections.unmodifiableCollection(employees.values());
    }

    public void setStatus(String employeeId, EmployeeStatus s) { get(employeeId).setStatus(s); }

    /** Rule M4: prevent circular reporting using DFS in the reporting graph. */
    public void setManager(String employeeId, String managerId) {
        Employee employee = get(employeeId);
        if (managerId == null) {
            employee.setManagerId(null);
            return;
        }
        if (managerId.equals(employeeId)) {
            throw new CircularReportingException("An employee cannot be their own manager.");
        }
        Employee mgr = get(managerId);
        if (!mgr.isActive()) {
            throw new EMSException("Manager must be an ACTIVE employee.");
        }
        // Walk up FROM the manager. If we encounter the employee, it'd create a cycle.
        String current = managerId;
        Set<String> visited = new HashSet<>();
        while (current != null) {
            if (!visited.add(current)) {
                // existing cycle - shouldn't happen if invariant maintained
                throw new CircularReportingException("Existing cycle detected at " + current);
            }
            if (current.equals(employeeId)) {
                throw new CircularReportingException(
                        "Setting manager=" + managerId + " for " + employeeId + " creates a cycle.");
            }
            Employee e = employees.get(current);
            current = e == null ? null : e.getManagerId();
        }
        employee.setManagerId(managerId);
    }

    public void transferDepartment(String employeeId, String newDepartmentId) {
        Employee e = get(employeeId);
        Department newDept = departmentService.get(newDepartmentId);
        String oldDept = e.getDepartmentId();
        e.setDepartmentId(newDepartmentId);
        newDept.addTransferLog(employeeId, oldDept);
    }

    /** Return all direct reports of a manager. */
    public List<Employee> directReports(String managerId) {
        List<Employee> out = new ArrayList<>();
        for (Employee e : employees.values()) {
            if (managerId.equals(e.getManagerId())) out.add(e);
        }
        return out;
    }

    /** Check if reportingMgr is in the chain above employee (anywhere). */
    public boolean isInReportingChain(String employeeId, String potentialManagerId) {
        String current = employees.get(employeeId) == null ? null : employees.get(employeeId).getManagerId();
        Set<String> visited = new HashSet<>();
        while (current != null && visited.add(current)) {
            if (current.equals(potentialManagerId)) return true;
            current = employees.get(current).getManagerId();
        }
        return false;
    }
}
