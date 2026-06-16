package com.hospital.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.enums.LabTestStatus;
import com.hospital.exception.HospitalException;
import com.hospital.exception.NotFoundException;
import com.hospital.model.LabOrder;

public class LabService {

    private final AtomicLong seq = new AtomicLong(3000);
    private final Map<String, LabOrder> orders = new ConcurrentHashMap<>();

    private final DoctorService doctorService;
    private final NotificationService notificationService;

    public LabService(DoctorService doctorService, NotificationService notificationService) {
        this.doctorService = doctorService;
        this.notificationService = notificationService;
    }

    public LabOrder order(String patientId, String doctorId, List<String> testNames) {
        doctorService.get(doctorId); // throws if doctor doesn't exist
        String id = "LAB-" + seq.incrementAndGet();
        LabOrder o = new LabOrder(id, patientId, doctorId, testNames);
        orders.put(id, o);
        return o;
    }

    public void startProcessing(String orderId) {
        LabOrder o = orders.get(orderId);
        if (o == null) throw new NotFoundException("Lab order not found: " + orderId);
        if (o.getStatus() != LabTestStatus.PENDING) {
            throw new HospitalException("Lab order " + orderId + " is not PENDING.");
        }
        o.setStatus(LabTestStatus.IN_PROGRESS);
    }

    public void complete(String orderId, String results) {
        LabOrder o = orders.get(orderId);
        if (o == null) throw new NotFoundException("Lab order not found: " + orderId);
        o.setResults(results);
        notificationService.notify(o.getPatientId(), "Lab report ready for " + orderId);
    }

    public Collection<LabOrder> getAll() { return Collections.unmodifiableCollection(orders.values()); }
}
