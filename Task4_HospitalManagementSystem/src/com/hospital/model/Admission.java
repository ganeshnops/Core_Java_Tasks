package com.hospital.model;

import java.time.LocalDateTime;

public class Admission {

    private final String id;
    private final String patientId;
    private final String bedId;
    private final LocalDateTime admittedAt;
    private volatile LocalDateTime dischargedAt;

    public Admission(String id, String patientId, String bedId) {
        this.id = id;
        this.patientId = patientId;
        this.bedId = bedId;
        this.admittedAt = LocalDateTime.now();
    }

    public String getId()                 { return id; }
    public String getPatientId()          { return patientId; }
    public String getBedId()              { return bedId; }
    public LocalDateTime getAdmittedAt()  { return admittedAt; }
    public LocalDateTime getDischargedAt(){ return dischargedAt; }

    public void discharge() { this.dischargedAt = LocalDateTime.now(); }
    public boolean isActive() { return dischargedAt == null; }

    @Override
    public String toString() {
        return String.format("%s | patient=%s | bed=%s | %s -> %s",
                id, patientId, bedId, admittedAt, dischargedAt == null ? "active" : dischargedAt);
    }
}
