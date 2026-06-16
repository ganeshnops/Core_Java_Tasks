package com.hospital.model;

import java.time.LocalDateTime;

import com.hospital.enums.AppointmentStatus;

/**
 * Appointment.
 *  - Rule A1: unique ID
 *  - Rule A4: fixed duration (default 15 min)
 *  - Rule A6: walk-ins supported (isWalkIn flag)
 */
public class Appointment {

    public static final int DURATION_MINUTES = 15;

    private final String id;
    private final String patientId;
    private final String doctorId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final boolean walkIn;
    private volatile AppointmentStatus status;

    public Appointment(String id, String patientId, String doctorId,
                       LocalDateTime startTime, boolean walkIn) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(DURATION_MINUTES);
        this.walkIn = walkIn;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public String getId()                  { return id; }
    public String getPatientId()           { return patientId; }
    public String getDoctorId()            { return doctorId; }
    public LocalDateTime getStartTime()    { return startTime; }
    public LocalDateTime getEndTime()      { return endTime; }
    public boolean isWalkIn()              { return walkIn; }
    public AppointmentStatus getStatus()   { return status; }

    public void setStatus(AppointmentStatus s) { this.status = s; }

    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    @Override
    public String toString() {
        return String.format("%s | patient=%s | doctor=%s | %s -> %s | %s | walkIn=%s",
                id, patientId, doctorId, startTime, endTime, status, walkIn);
    }
}
