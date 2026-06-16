package com.hospital.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.exception.NotFoundException;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;

public class BillingService {

    private final AtomicLong seq = new AtomicLong(9000);
    private final Map<String, Bill> bills = new ConcurrentHashMap<>();

    private final DoctorService doctorService;
    private final Map<String, Double> dailyRevenue = new ConcurrentHashMap<>();
    private final Map<String, Double> departmentRevenue = new ConcurrentHashMap<>();
    private final Map<String, Double> doctorRevenue = new ConcurrentHashMap<>();
    private double insurancePaymentsTotal = 0;
    private final Object revLock = new Object();

    public BillingService(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    public Bill createBill(String patientId) {
        String id = "BIL-" + seq.incrementAndGet();
        Bill b = new Bill(id, patientId);
        bills.put(id, b);
        return b;
    }

    public Bill get(String id) {
        Bill b = bills.get(id);
        if (b == null) throw new NotFoundException("Bill not found: " + id);
        return b;
    }

    /** Add doctor consultation charge (Rule Billing 2). */
    public void addConsultationCharge(String billId, String doctorId) {
        Doctor d = doctorService.get(doctorId);
        Bill b = get(billId);
        b.addItem("Consultation - " + d.getName(), d.getConsultFee());
    }

    public void addRoomCharge(String billId, double amount, int days) {
        get(billId).addItem("Room charge (" + days + " days)", amount * days);
    }

    public void addLabCharge(String billId, String testName, double amount) {
        get(billId).addItem("Lab - " + testName, amount);
    }

    public void addPharmacyCharge(String billId, String medicineName, double amount) {
        get(billId).addItem("Pharmacy - " + medicineName, amount);
    }

    public void applyDiscount(String billId, double amount, String authorizedBy) {
        get(billId).applyDiscount(amount, authorizedBy);
    }

    public void pay(String billId, double insurancePaid) {
        Bill b = get(billId);
        b.markPaid();
        synchronized (revLock) {
            String day = b.getCreatedAt().toLocalDate().toString();
            double net = b.getTotal();
            dailyRevenue.merge(day, net - insurancePaid, Double::sum);
            insurancePaymentsTotal += insurancePaid;
        }
    }

    public Collection<Bill> getAll() { return Collections.unmodifiableCollection(bills.values()); }

    public Map<String, Double> getDailyRevenue()       { return Collections.unmodifiableMap(dailyRevenue); }
    public Map<String, Double> getDepartmentRevenue()  { return Collections.unmodifiableMap(departmentRevenue); }
    public Map<String, Double> getDoctorRevenue()      { return Collections.unmodifiableMap(doctorRevenue); }
    public double getInsurancePaymentsTotal() {
        synchronized (revLock) { return insurancePaymentsTotal; }
    }
}
