package com.ems.model;

import java.time.LocalDate;
import java.time.YearMonth;

import com.ems.enums.PayrollStatus;
import com.ems.exception.EMSException;

public class Payslip {

    private final String id;
    private final String employeeId;
    private final YearMonth period;
    private final double basic;
    private final double hra;
    private final double allowances;
    private final double bonus;
    private final double providentFund;   // 12% of basic
    private final double tax;              // 10% of (gross - PF) demo
    private final double netSalary;
    private final LocalDate generatedOn;
    private volatile PayrollStatus status;

    public Payslip(String id, String employeeId, YearMonth period,
                   double basic, double hra, double allowances, double bonus) {
        this.id = id;
        this.employeeId = employeeId;
        this.period = period;
        this.basic = basic;
        this.hra = hra;
        this.allowances = allowances;
        this.bonus = bonus;
        this.providentFund = round(basic * 0.12);
        double gross = basic + hra + allowances + bonus;
        this.tax = round((gross - providentFund) * 0.10);
        this.netSalary = round(gross - providentFund - tax);
        this.generatedOn = LocalDate.now();
        this.status = PayrollStatus.DRAFT;
    }

    public String getId()              { return id; }
    public String getEmployeeId()      { return employeeId; }
    public YearMonth getPeriod()       { return period; }
    public double getBasic()           { return basic; }
    public double getHra()             { return hra; }
    public double getAllowances()      { return allowances; }
    public double getBonus()           { return bonus; }
    public double getProvidentFund()   { return providentFund; }
    public double getTax()             { return tax; }
    public double getNetSalary()       { return netSalary; }
    public LocalDate getGeneratedOn()  { return generatedOn; }
    public PayrollStatus getStatus()   { return status; }

    public void process() {
        if (status == PayrollStatus.PROCESSED) {
            throw new EMSException("Payslip already PROCESSED - cannot modify.");
        }
        this.status = PayrollStatus.PROCESSED;
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | basic=%.2f hra=%.2f allow=%.2f bonus=%.2f pf=%.2f tax=%.2f NET=%.2f | %s",
                id, employeeId, period, basic, hra, allowances, bonus, providentFund, tax, netSalary, status);
    }
}
