package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hospital.enums.LabTestStatus;
import com.hospital.exception.HospitalException;

/**
 * Lab order.
 *  - Rule Lab 1: unique ID
 *  - Rule 2: only authorized doctors can order (checked in LabService)
 *  - Rule 3: linked to patient
 *  - Rule 4: status PENDING -> IN_PROGRESS -> COMPLETED
 *  - Rule 5: completed reports immutable
 */
public class LabOrder {

    private final String id;
    private final String patientId;
    private final String doctorId;
    private final List<String> testNames;
    private volatile LabTestStatus status;
    private volatile String results;
    private final LocalDateTime orderedAt;
    private volatile LocalDateTime completedAt;

    public LabOrder(String id, String patientId, String doctorId, List<String> testNames) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testNames = new ArrayList<>(testNames);
        this.status = LabTestStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
    }

    public String getId()                 { return id; }
    public String getPatientId()          { return patientId; }
    public String getDoctorId()           { return doctorId; }
    public List<String> getTestNames()    { return Collections.unmodifiableList(testNames); }
    public LabTestStatus getStatus()      { return status; }
    public String getResults()            { return results; }
    public LocalDateTime getOrderedAt()   { return orderedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setStatus(LabTestStatus s) { this.status = s; }

    public void setResults(String r) {
        if (status == LabTestStatus.COMPLETED) {
            throw new HospitalException("Completed lab report cannot be modified (Rule Lab 5).");
        }
        this.results = r;
        this.status = LabTestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("%s | patient=%s | doctor=%s | tests=%s | %s",
                id, patientId, doctorId, testNames, status);
    }
}
