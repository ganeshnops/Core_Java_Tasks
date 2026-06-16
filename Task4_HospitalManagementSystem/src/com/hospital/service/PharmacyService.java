package com.hospital.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hospital.exception.NotFoundException;
import com.hospital.exception.PharmacyException;
import com.hospital.model.Medicine;
import com.hospital.model.Prescription;

public class PharmacyService {

    private final Map<String, Medicine> medicines = new ConcurrentHashMap<>();

    public Medicine add(Medicine m) { medicines.put(m.getId(), m); return m; }
    public Medicine get(String id) {
        Medicine m = medicines.get(id);
        if (m == null) throw new NotFoundException("Medicine not found: " + id);
        return m;
    }
    public Collection<Medicine> getAll() { return Collections.unmodifiableCollection(medicines.values()); }

    /** Sell with optional prescription. Returns total amount. */
    public double sell(String medicineId, int qty, Prescription prescription) {
        Medicine m = get(medicineId);
        if (m.isExpired()) {
            throw new PharmacyException("Medicine expired: " + medicineId);
        }
        if (m.isPrescriptionRequired() && (prescription == null
                || !prescription.getMedicines().containsKey(medicineId))) {
            throw new PharmacyException("Prescription required for " + medicineId);
        }
        if (!m.sell(qty)) {
            throw new PharmacyException("Not enough stock for " + medicineId);
        }
        if (m.isLowStock()) {
            System.out.println("  [pharmacy alert] LOW STOCK: " + m.getName() + " - " + m.getStock() + " left");
        }
        return qty * m.getPrice();
    }
}
