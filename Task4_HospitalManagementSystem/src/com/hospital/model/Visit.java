package com.hospital.model;

import java.time.LocalDateTime;

/** One outpatient visit to a doctor. */
public class Visit {

    private final String patientId;
    private final String doctorId;
    private final LocalDateTime visitTime;
    private final String notes;

    public Visit(String patientId, String doctorId, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitTime = LocalDateTime.now();
        this.notes = notes;
    }

    public String getPatientId() { return patientId; }
    public String getDoctorId()  { return doctorId; }
    public LocalDateTime getVisitTime() { return visitTime; }
    public String getNotes()     { return notes; }

    @Override
    public String toString() {
        return patientId + " -> " + doctorId + " | " + visitTime + " | " + notes;
    }
}
