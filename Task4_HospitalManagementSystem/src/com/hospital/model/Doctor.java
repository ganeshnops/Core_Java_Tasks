package com.hospital.model;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hospital.enums.DoctorStatus;

/**
 * Doctor entity.
 *  - Doctor Rule 1: unique ID
 *  - 2: belongs to a department
 *  - 3: can have multiple specializations
 *  - 4: predefined consultation timings (consultStart - consultEnd)
 *  - 6,7: leave handling - if status = ON_LEAVE, no new appointments
 */
public class Doctor {

    private final String id;
    private final String name;
    private final String departmentId;
    private final List<String> specializations = new CopyOnWriteArrayList<>();
    private final LocalTime consultStart;
    private final LocalTime consultEnd;
    private final int consultFee;
    private volatile DoctorStatus status;

    public Doctor(String id, String name, String departmentId,
                  LocalTime start, LocalTime end, int consultFee) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.consultStart = start;
        this.consultEnd = end;
        this.consultFee = consultFee;
        this.status = DoctorStatus.AVAILABLE;
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getDepartmentId()    { return departmentId; }
    public List<String> getSpecializations() { return Collections.unmodifiableList(specializations); }
    public LocalTime getConsultStart() { return consultStart; }
    public LocalTime getConsultEnd()   { return consultEnd; }
    public int getConsultFee()         { return consultFee; }
    public DoctorStatus getStatus()    { return status; }

    public void setStatus(DoctorStatus s)       { this.status = s; }
    public void addSpecialization(String spec)  { specializations.add(spec); }

    public boolean isAvailable()       { return status == DoctorStatus.AVAILABLE; }

    public boolean isWithinConsultTime(LocalTime t) {
        return !t.isBefore(consultStart) && t.isBefore(consultEnd);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | dept=%s | %s-%s | fee=Rs.%d | %s | specs=%s",
                id, name, departmentId, consultStart, consultEnd, consultFee, status, specializations);
    }
}
