package com.hospital.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hospital.enums.DoctorStatus;
import com.hospital.exception.NotFoundException;
import com.hospital.model.Department;
import com.hospital.model.Doctor;

public class DoctorService {

    private final Map<String, Doctor> doctors = new ConcurrentHashMap<>();
    private final Map<String, Department> departments = new ConcurrentHashMap<>();

    public Department addDepartment(Department d) {
        departments.put(d.getId(), d);
        return d;
    }
    public Doctor add(Doctor d) { doctors.put(d.getId(), d); return d; }

    public Doctor get(String id) {
        Doctor d = doctors.get(id);
        if (d == null) throw new NotFoundException("Doctor not found: " + id);
        return d;
    }

    public Collection<Doctor> getAll()        { return Collections.unmodifiableCollection(doctors.values()); }
    public Collection<Department> getAllDepartments() { return Collections.unmodifiableCollection(departments.values()); }

    public void applyLeave(String doctorId)    { get(doctorId).setStatus(DoctorStatus.ON_LEAVE); }
    public void resumeDuty(String doctorId)    { get(doctorId).setStatus(DoctorStatus.AVAILABLE); }
}
