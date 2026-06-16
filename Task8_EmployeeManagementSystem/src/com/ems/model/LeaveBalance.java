package com.ems.model;

import java.util.EnumMap;
import java.util.Map;

import com.ems.enums.LeaveType;

public class LeaveBalance {

    private final String employeeId;
    private final Map<LeaveType, Double> balances = new EnumMap<>(LeaveType.class);

    public LeaveBalance(String employeeId) {
        this.employeeId = employeeId;
        // Default annual allocations
        balances.put(LeaveType.CASUAL, 12.0);
        balances.put(LeaveType.SICK,   12.0);
        balances.put(LeaveType.EARNED, 15.0);
    }

    public String getEmployeeId() { return employeeId; }

    public synchronized double get(LeaveType type) {
        return balances.getOrDefault(type, 0.0);
    }

    public synchronized boolean deduct(LeaveType type, double days) {
        double current = balances.getOrDefault(type, 0.0);
        if (current < days) return false;
        balances.put(type, current - days);
        return true;
    }

    public synchronized void restore(LeaveType type, double days) {
        balances.merge(type, days, Double::sum);
    }

    @Override
    public synchronized String toString() {
        return employeeId + " | balances=" + balances;
    }
}
