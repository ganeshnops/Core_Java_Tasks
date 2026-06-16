package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hospital.enums.SurgeryStatus;

public class Surgery {

    private final String id;
    private final String patientId;
    private final String otId;
    private final List<String> surgeonIds;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private volatile SurgeryStatus status;
    private volatile String notes;

    public Surgery(String id, String patientId, String otId, List<String> surgeonIds,
                   LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.patientId = patientId;
        this.otId = otId;
        this.surgeonIds = new ArrayList<>(surgeonIds);
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SurgeryStatus.SCHEDULED;
    }

    public String getId()                  { return id; }
    public String getPatientId()           { return patientId; }
    public String getOtId()                { return otId; }
    public List<String> getSurgeonIds()    { return Collections.unmodifiableList(surgeonIds); }
    public LocalDateTime getStartTime()    { return startTime; }
    public LocalDateTime getEndTime()      { return endTime; }
    public SurgeryStatus getStatus()       { return status; }
    public String getNotes()               { return notes; }

    public void setStatus(SurgeryStatus s) { this.status = s; }
    public void setNotes(String n)         { this.notes = n; }

    @Override
    public String toString() {
        return String.format("%s | patient=%s | OT=%s | surgeons=%s | %s -> %s | %s",
                id, patientId, otId, surgeonIds, startTime, endTime, status);
    }
}
