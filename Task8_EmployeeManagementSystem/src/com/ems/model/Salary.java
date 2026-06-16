package com.ems.model;

import com.ems.exception.EMSException;

public class Salary {

    private final String employeeId;
    private final double basic;
    private final double hra;          // House Rent Allowance
    private final double allowances;

    public Salary(String employeeId, double basic, double hra, double allowances) {
        if (basic <= 0) throw new EMSException("Basic salary must be > 0");
        this.employeeId = employeeId;
        this.basic = basic;
        this.hra = hra;
        this.allowances = allowances;
    }

    public String getEmployeeId() { return employeeId; }
    public double getBasic()      { return basic; }
    public double getHra()        { return hra; }
    public double getAllowances() { return allowances; }
    public double getGross()      { return basic + hra + allowances; }
}
