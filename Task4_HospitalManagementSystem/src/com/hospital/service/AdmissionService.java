package com.hospital.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.enums.BedType;
import com.hospital.enums.PatientType;
import com.hospital.exception.HospitalException;
import com.hospital.model.Admission;
import com.hospital.model.Bed;
import com.hospital.model.Patient;

public class AdmissionService {

    private final AtomicLong seq = new AtomicLong(5000);
    private final Map<String, Admission> admissions = new ConcurrentHashMap<>();

    private final PatientService patientService;
    private final BedService bedService;
    private final NotificationService notificationService;

    public AdmissionService(PatientService ps, BedService bs, NotificationService ns) {
        this.patientService = ps;
        this.bedService = bs;
        this.notificationService = ns;
    }

    public Admission admit(String patientId, BedType type) {
        Patient p = patientService.get(patientId);

        // ICU eligibility (Rule Bed 4) - emergency or age>=60 or has "critical" history note
        if (type == BedType.ICU && p.getType() != PatientType.EMERGENCY && p.getAge() < 60) {
            throw new HospitalException("ICU bed only for emergency / age>=60.");
        }
        Bed bed = bedService.assignBedOfType(type, patientId);
        String id = "ADM-" + seq.incrementAndGet();
        Admission adm = new Admission(id, patientId, bed.getId());
        admissions.put(id, adm);
        notificationService.notify(patientId, "Admitted as " + id + " to bed " + bed.getId());
        return adm;
    }

    public void discharge(String admissionId) {
        Admission adm = admissions.get(admissionId);
        if (adm == null) throw new HospitalException("Admission not found.");
        if (!adm.isActive()) throw new HospitalException("Already discharged: " + admissionId);
        adm.discharge();
        bedService.release(adm.getBedId());
        notificationService.notify(adm.getPatientId(), "Discharged from " + admissionId);
    }

    public Admission get(String id) { return admissions.get(id); }
    public java.util.Collection<Admission> getAll() {
        return java.util.Collections.unmodifiableCollection(admissions.values());
    }
}
