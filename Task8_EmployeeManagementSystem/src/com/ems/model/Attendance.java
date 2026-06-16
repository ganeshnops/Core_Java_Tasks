package com.ems.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import com.ems.enums.AttendanceStatus;

public class Attendance {

    private final String employeeId;
    private final LocalDate date;
    private final LocalDateTime checkInTime;
    private volatile LocalDateTime checkOutTime;
    private final boolean late;
    private volatile AttendanceStatus status;

    public Attendance(String employeeId, LocalDate date, LocalDateTime checkIn) {
        this.employeeId = employeeId;
        this.date = date;
        this.checkInTime = checkIn;
        this.late = checkIn.toLocalTime().isAfter(LocalTime.of(9, 30));
        this.status = AttendanceStatus.PRESENT;
    }

    public String getEmployeeId()           { return employeeId; }
    public LocalDate getDate()              { return date; }
    public LocalDateTime getCheckInTime()   { return checkInTime; }
    public LocalDateTime getCheckOutTime()  { return checkOutTime; }
    public boolean isLate()                 { return late; }
    public AttendanceStatus getStatus()     { return status; }

    public void setCheckOutTime(LocalDateTime t) { this.checkOutTime = t; }
    public void setStatus(AttendanceStatus s)    { this.status = s; }

    public long getHoursWorked() {
        if (checkOutTime == null) return 0;
        return ChronoUnit.HOURS.between(checkInTime, checkOutTime);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | in=%s | out=%s | %s%s",
                employeeId, date, checkInTime.toLocalTime(),
                checkOutTime == null ? "-MISSING-" : checkOutTime.toLocalTime(),
                status, late ? " | LATE" : "");
    }
}
