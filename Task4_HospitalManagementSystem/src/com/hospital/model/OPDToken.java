package com.hospital.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.enums.ConsultationStatus;

/**
 * OPD token.
 *  - Implements Comparable so PriorityBlockingQueue can sort by priority + tokenNumber.
 *  - Emergency patients get higher priority (lower priority value = higher in queue).
 */
public class OPDToken implements Comparable<OPDToken> {

    /** Priority value - LOWER = HIGHER priority. Emergency = 0, Regular = 1. */
    private final int priority;
    private final long tokenNumber;
    private final String patientId;
    private final String doctorId;
    private final LocalDateTime issuedAt;
    private volatile ConsultationStatus status;

    private static final AtomicLong SEQ = new AtomicLong(0);

    public OPDToken(String patientId, String doctorId, boolean emergency) {
        this.tokenNumber = SEQ.incrementAndGet();
        this.priority = emergency ? 0 : 1;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.issuedAt = LocalDateTime.now();
        this.status = ConsultationStatus.WAITING;
    }

    public long getTokenNumber()         { return tokenNumber; }
    public int getPriority()             { return priority; }
    public String getPatientId()         { return patientId; }
    public String getDoctorId()          { return doctorId; }
    public LocalDateTime getIssuedAt()   { return issuedAt; }
    public ConsultationStatus getStatus(){ return status; }

    public void setStatus(ConsultationStatus s) { this.status = s; }

    @Override
    public int compareTo(OPDToken o) {
        // 1) lower priority value first (emergency=0 before regular=1)
        // 2) within same priority, lower tokenNumber first (FIFO)
        if (this.priority != o.priority) return Integer.compare(this.priority, o.priority);
        return Long.compare(this.tokenNumber, o.tokenNumber);
    }

    @Override
    public String toString() {
        return String.format("TOKEN-%d | %s | patient=%s | doctor=%s | %s | %s",
                tokenNumber, priority == 0 ? "EMERGENCY" : "REGULAR",
                patientId, doctorId, status, issuedAt);
    }
}
