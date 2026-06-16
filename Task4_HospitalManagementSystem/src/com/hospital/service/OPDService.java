package com.hospital.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import com.hospital.enums.ConsultationStatus;
import com.hospital.enums.PatientType;
import com.hospital.model.OPDToken;
import com.hospital.model.Patient;

/**
 * OPD token queue.
 *  - Rule O1, O2: tokens auto + sequential.
 *  - Rule O3: emergency patients get higher priority - via PriorityBlockingQueue.
 *  - Rule O4: missed tokens handled (status MISSED).
 *  - Rule O5: status tracked.
 */
public class OPDService {

    private final PriorityBlockingQueue<OPDToken> queue = new PriorityBlockingQueue<>();
    private final List<OPDToken> issued = new ArrayList<>();

    private final PatientService patientService;

    public OPDService(PatientService patientService) {
        this.patientService = patientService;
    }

    public synchronized OPDToken issueToken(String patientId, String doctorId) {
        Patient p = patientService.get(patientId);
        OPDToken token = new OPDToken(patientId, doctorId, p.getType() == PatientType.EMERGENCY);
        queue.offer(token);
        issued.add(token);
        return token;
    }

    /** Pull the next token (emergency-first, then FIFO). */
    public OPDToken pollNext() {
        return queue.poll();
    }

    public void markMissed(OPDToken t) { t.setStatus(ConsultationStatus.MISSED); }
    public void markInProgress(OPDToken t) { t.setStatus(ConsultationStatus.IN_PROGRESS); }
    public void markCompleted(OPDToken t)  { t.setStatus(ConsultationStatus.COMPLETED); }

    public List<OPDToken> getIssued() { return new ArrayList<>(issued); }
    public int queueSize() { return queue.size(); }
}
