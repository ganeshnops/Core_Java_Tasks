package com.hospital.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hospital.enums.BedStatus;
import com.hospital.enums.BedType;
import com.hospital.exception.NoBedAvailableException;
import com.hospital.exception.NotFoundException;
import com.hospital.model.Bed;

public class BedService {

    private final Map<String, Bed> beds = new ConcurrentHashMap<>();
    private final Object assignLock = new Object();

    public Bed addBed(Bed b) { beds.put(b.getId(), b); return b; }
    public Bed get(String id) {
        Bed b = beds.get(id);
        if (b == null) throw new NotFoundException("Bed not found: " + id);
        return b;
    }

    public Collection<Bed> getAll() { return Collections.unmodifiableCollection(beds.values()); }

    /**
     * Atomically assign an available bed of the requested type to a patient.
     *  - synchronized on assignLock to prevent two patients getting same bed.
     */
    public Bed assignBedOfType(BedType type, String patientId) {
        synchronized (assignLock) {
            for (Bed b : beds.values()) {
                if (b.getType() == type && b.getStatus() == BedStatus.AVAILABLE) {
                    b.setStatus(BedStatus.OCCUPIED);
                    b.setCurrentPatientId(patientId);
                    b.addTransferLog("ASSIGNED to " + patientId + " at " + LocalDateTime.now());
                    return b;
                }
            }
            throw new NoBedAvailableException("No " + type + " bed available.");
        }
    }

    public void release(String bedId) {
        synchronized (assignLock) {
            Bed b = get(bedId);
            b.addTransferLog("RELEASED from " + b.getCurrentPatientId() + " at " + LocalDateTime.now());
            b.setStatus(BedStatus.AVAILABLE);
            b.setCurrentPatientId(null);
        }
    }

    public void setMaintenance(String bedId, boolean inMaintenance) {
        Bed b = get(bedId);
        b.setStatus(inMaintenance ? BedStatus.MAINTENANCE : BedStatus.AVAILABLE);
    }
}
