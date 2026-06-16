package com.hospital.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hospital.enums.BedStatus;
import com.hospital.enums.BedType;

/**
 * Hospital bed.
 *  - Rule Bed 2: status (AVAILABLE/OCCUPIED/MAINTENANCE).
 *  - Rule Bed 3: occupied beds cannot be reassigned.
 *  - Rule Bed 4: ICU beds only to eligible patients.
 *  - Rule Bed 5: transfer history maintained.
 */
public class Bed {

    private final String id;
    private final BedType type;
    private final String wardId;
    private volatile BedStatus status;
    private volatile String currentPatientId;
    private final List<String> transferHistory = new ArrayList<>();

    public Bed(String id, BedType type, String wardId) {
        this.id = id;
        this.type = type;
        this.wardId = wardId;
        this.status = BedStatus.AVAILABLE;
    }

    public String getId()                  { return id; }
    public BedType getType()               { return type; }
    public String getWardId()              { return wardId; }
    public BedStatus getStatus()           { return status; }
    public String getCurrentPatientId()    { return currentPatientId; }
    public List<String> getTransferHistory() { return Collections.unmodifiableList(transferHistory); }

    public void setStatus(BedStatus s)               { this.status = s; }
    public void setCurrentPatientId(String pid)      { this.currentPatientId = pid; }
    public void addTransferLog(String log)           { transferHistory.add(log); }

    public boolean isAvailable() { return status == BedStatus.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("%s | %s | ward=%s | %s | patient=%s",
                id, type, wardId, status, currentPatientId == null ? "-" : currentPatientId);
    }
}
