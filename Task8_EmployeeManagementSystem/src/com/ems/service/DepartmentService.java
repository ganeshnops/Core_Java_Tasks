package com.ems.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.exception.DuplicateException;
import com.ems.exception.NotFoundException;
import com.ems.model.Department;

public class DepartmentService {

    private final AtomicLong seq = new AtomicLong(100);
    private final Map<String, Department> departments = new ConcurrentHashMap<>();
    private final Set<String> usedNames = ConcurrentHashMap.newKeySet();

    public Department add(String name) {
        if (!usedNames.add(name)) throw new DuplicateException("Department name exists: " + name);
        String id = "D" + seq.incrementAndGet();
        Department d = new Department(id, name);
        departments.put(id, d);
        return d;
    }

    public Department get(String id) {
        Department d = departments.get(id);
        if (d == null) throw new NotFoundException("Department not found: " + id);
        return d;
    }

    public Collection<Department> getAll() {
        return Collections.unmodifiableCollection(departments.values());
    }

    public void setManager(String departmentId, String managerId) {
        get(departmentId).setManagerId(managerId);
    }
}
