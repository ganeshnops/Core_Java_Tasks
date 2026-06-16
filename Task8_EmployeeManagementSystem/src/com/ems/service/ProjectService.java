package com.ems.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.exception.EMSException;
import com.ems.exception.NotFoundException;
import com.ems.model.Employee;
import com.ems.model.Project;

public class ProjectService {

    private final AtomicLong seq = new AtomicLong(2000);
    private final Map<String, Project> projects = new ConcurrentHashMap<>();

    private final EmployeeService employeeService;
    private final NotificationService notify;

    public ProjectService(EmployeeService es, NotificationService notify) {
        this.employeeService = es;
        this.notify = notify;
    }

    public Project create(String name, LocalDate startDate, LocalDate endDate) {
        String id = "PROJ-" + seq.incrementAndGet();
        Project p = new Project(id, name, startDate, endDate);
        projects.put(id, p);
        return p;
    }

    public Project get(String id) {
        Project p = projects.get(id);
        if (p == null) throw new NotFoundException("Project not found: " + id);
        return p;
    }

    public void assignEmployee(String projectId, String employeeId, String managerId) {
        Project p = get(projectId);
        Employee e = employeeService.get(employeeId);
        if (!e.isActive()) throw new EMSException("Only ACTIVE employees can be assigned.");
        if (!p.assign(employeeId)) {
            throw new EMSException("Employee " + employeeId + " already assigned to " + projectId);
        }
        notify.notify(managerId, "Assigned " + employeeId + " to project " + projectId);
        notify.notify(employeeId, "You were assigned to project " + projectId);
    }

    public List<Project> byEmployee(String employeeId) {
        List<Project> out = new ArrayList<>();
        for (Project p : projects.values()) {
            if (p.getAssignedEmployees().contains(employeeId)) out.add(p);
        }
        return out;
    }

    public Collection<Project> getAll() { return Collections.unmodifiableCollection(projects.values()); }
}
