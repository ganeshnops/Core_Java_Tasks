package com.hospital.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Prescription {

    private final String id;
    private final String patientId;
    private final String doctorId;
    private final Map<String, Integer> medicines = new LinkedHashMap<>();   // medicineId -> qty
    private final LocalDateTime issuedAt;

    public Prescription(String id, String patientId, String doctorId) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.issuedAt = LocalDateTime.now();
    }

    public String getId()             { return id; }
    public String getPatientId()      { return patientId; }
    public String getDoctorId()       { return doctorId; }
    public Map<String, Integer> getMedicines() { return Collections.unmodifiableMap(medicines); }
    public LocalDateTime getIssuedAt(){ return issuedAt; }

    public void addMedicine(String medicineId, int qty) { medicines.put(medicineId, qty); }
}
