package com.hospital.model;

import java.time.LocalTime;

import com.hospital.enums.StaffRole;

public class Staff {

    private final String id;
    private final String name;
    private final StaffRole role;
    private final LocalTime shiftStart;
    private final LocalTime shiftEnd;

    public Staff(String id, String name, StaffRole role, LocalTime shiftStart, LocalTime shiftEnd) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public StaffRole getRole()        { return role; }
    public LocalTime getShiftStart()  { return shiftStart; }
    public LocalTime getShiftEnd()    { return shiftEnd; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s-%s", id, name, role, shiftStart, shiftEnd);
    }
}
