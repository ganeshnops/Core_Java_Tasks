package com.ems.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ems.exception.EMSException;
import com.ems.model.Attendance;

public class AttendanceService {

    /** key = "employeeId|YYYY-MM-DD" */
    private final Map<String, Attendance> records = new ConcurrentHashMap<>();

    public Attendance checkIn(String employeeId) {
        LocalDate today = LocalDate.now();
        String key = key(employeeId, today);
        if (records.containsKey(key)) {
            throw new EMSException("Already checked in today.");
        }
        Attendance a = new Attendance(employeeId, today, LocalDateTime.now());
        records.put(key, a);
        return a;
    }

    public Attendance checkOut(String employeeId) {
        LocalDate today = LocalDate.now();
        Attendance a = records.get(key(employeeId, today));
        if (a == null) throw new EMSException("Not checked in today.");
        if (a.getCheckOutTime() != null) {
            throw new EMSException("Already checked out today.");
        }
        a.setCheckOutTime(LocalDateTime.now());
        return a;
    }

    public Collection<Attendance> getAll() { return Collections.unmodifiableCollection(records.values()); }

    /** Monthly attendance for an employee. */
    public List<Attendance> monthly(String employeeId, YearMonth month) {
        List<Attendance> out = new ArrayList<>();
        for (Attendance a : records.values()) {
            if (a.getEmployeeId().equals(employeeId)
                    && YearMonth.from(a.getDate()).equals(month)) {
                out.add(a);
            }
        }
        out.sort((x, y) -> x.getDate().compareTo(y.getDate()));
        return out;
    }

    /** Missing check-outs - someone forgot to check out. */
    public List<Attendance> findMissingCheckouts() {
        List<Attendance> out = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Attendance a : records.values()) {
            if (a.getDate().isBefore(today) && a.getCheckOutTime() == null) {
                out.add(a);
            }
        }
        return out;
    }

    private static String key(String emp, LocalDate date) {
        return emp + "|" + date;
    }
}
