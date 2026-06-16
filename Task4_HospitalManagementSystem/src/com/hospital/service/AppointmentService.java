package com.hospital.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hospital.enums.AppointmentStatus;
import com.hospital.enums.DoctorStatus;
import com.hospital.exception.AppointmentConflictException;
import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;

public class AppointmentService {

    private final AtomicLong seq = new AtomicLong(1000);
    private final Map<String, Appointment> appointments = new ConcurrentHashMap<>();
    private final Object scheduleLock = new Object();

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final NotificationService notificationService;

    public AppointmentService(DoctorService doctorService, PatientService patientService,
                              NotificationService notificationService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.notificationService = notificationService;
    }

    public Appointment book(String patientId, String doctorId,
                            LocalDateTime startTime, boolean walkIn) {
        patientService.get(patientId);
        Doctor doctor = doctorService.get(doctorId);
        if (doctor.getStatus() == DoctorStatus.ON_LEAVE) {
            throw new HospitalException("Doctor " + doctorId + " is ON_LEAVE.");
        }
        if (!doctor.isWithinConsultTime(startTime.toLocalTime())) {
            throw new HospitalException("Outside doctor consultation hours " + doctor.getConsultStart()
                    + " - " + doctor.getConsultEnd());
        }
        LocalDateTime endTime = startTime.plusMinutes(Appointment.DURATION_MINUTES);

        synchronized (scheduleLock) {
            for (Appointment a : appointments.values()) {
                if (!a.getDoctorId().equals(doctorId)) continue;
                if (a.getStatus() == AppointmentStatus.CANCELLED) continue;
                if (a.overlaps(startTime, endTime)) {
                    throw new AppointmentConflictException("Doctor " + doctorId
                            + " already booked at " + a.getStartTime());
                }
            }
            String id = "APT-" + seq.incrementAndGet();
            Appointment apt = new Appointment(id, patientId, doctorId, startTime, walkIn);
            appointments.put(id, apt);
            notificationService.notify(patientId, "Appointment " + id + " booked with " + doctorId + " at " + startTime);
            return apt;
        }
    }

    public void cancel(String apptId) {
        Appointment a = appointments.get(apptId);
        if (a == null) throw new HospitalException("Appointment not found: " + apptId);
        a.setStatus(AppointmentStatus.CANCELLED);
        notificationService.notify(a.getPatientId(), "Appointment " + apptId + " CANCELLED");
    }

    public void markCompleted(String apptId) {
        Appointment a = appointments.get(apptId);
        if (a == null) throw new HospitalException("Appointment not found.");
        a.setStatus(AppointmentStatus.COMPLETED);
    }

    public Collection<Appointment> getAll() { return Collections.unmodifiableCollection(appointments.values()); }

    public List<Appointment> byPatient(String patientId) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : appointments.values()) if (a.getPatientId().equals(patientId)) out.add(a);
        return out;
    }
}
