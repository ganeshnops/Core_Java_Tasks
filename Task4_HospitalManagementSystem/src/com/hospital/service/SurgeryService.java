package com.hospital.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.exception.HospitalException;
import com.hospital.exception.OTBookingException;
import com.hospital.model.OperationTheatre;
import com.hospital.model.Surgery;

public class SurgeryService {

    private final AtomicLong seq = new AtomicLong(7000);
    private final Map<String, OperationTheatre> theatres = new ConcurrentHashMap<>();
    private final Map<String, Surgery> surgeries = new ConcurrentHashMap<>();
    private final Object bookLock = new Object();

    public OperationTheatre addTheatre(OperationTheatre ot) { theatres.put(ot.getId(), ot); return ot; }
    public OperationTheatre getTheatre(String id) {
        OperationTheatre ot = theatres.get(id);
        if (ot == null) throw new HospitalException("Theatre not found: " + id);
        return ot;
    }

    public Surgery schedule(String patientId, String otId, List<String> surgeonIds,
                            LocalDateTime startTime, LocalDateTime endTime) {
        if (surgeonIds == null || surgeonIds.isEmpty()) {
            throw new HospitalException("At least one surgeon must be assigned.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new HospitalException("Surgery start must be before end.");
        }
        OperationTheatre ot = getTheatre(otId);

        synchronized (bookLock) {
            if (ot.overlaps(startTime, endTime)) {
                throw new OTBookingException("OT " + otId + " is busy at this time slot.");
            }
            String id = "SUR-" + seq.incrementAndGet();
            Surgery s = new Surgery(id, patientId, otId, surgeonIds, startTime, endTime);
            ot.book(startTime, endTime, id);
            surgeries.put(id, s);
            return s;
        }
    }

    public Surgery get(String id) { return surgeries.get(id); }
    public Collection<Surgery> getAll() { return Collections.unmodifiableCollection(surgeries.values()); }
}
