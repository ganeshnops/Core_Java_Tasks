package com.hospital.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.enums.Gender;
import com.hospital.enums.PatientType;
import com.hospital.exception.DuplicateRegistrationException;
import com.hospital.exception.NotFoundException;
import com.hospital.model.Patient;

public class PatientService {

    private final AtomicLong seq = new AtomicLong(1000);
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();
    private final Set<String> usedMobiles = ConcurrentHashMap.newKeySet();
    private final AuditService auditService;

    public PatientService(AuditService auditService) {
        this.auditService = auditService;
    }

    public Patient register(String name, int age, Gender gender, String mobile,
                            String address, PatientType type) {
        if (type != PatientType.EMERGENCY && mobile != null && !usedMobiles.add(mobile)) {
            throw new DuplicateRegistrationException("Mobile already registered: " + mobile);
        }
        String id = "P" + seq.incrementAndGet();
        Patient p = new Patient(id, name, age, gender, mobile, address, type);
        patients.put(id, p);
        auditService.log("system", "REGISTER", "Patient " + id);
        return p;
    }

    public Patient get(String id) {
        Patient p = patients.get(id);
        if (p == null) throw new NotFoundException("Patient not found: " + id);
        return p;
    }

    public Collection<Patient> getAll() { return Collections.unmodifiableCollection(patients.values()); }
}
