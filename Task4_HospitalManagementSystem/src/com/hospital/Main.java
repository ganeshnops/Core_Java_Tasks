package com.hospital;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.hospital.enums.BedType;
import com.hospital.enums.Gender;
import com.hospital.enums.PatientType;
import com.hospital.exception.HospitalException;
import com.hospital.model.Admission;
import com.hospital.model.Appointment;
import com.hospital.model.Bed;
import com.hospital.model.Bill;
import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.InsurancePolicy;
import com.hospital.model.LabOrder;
import com.hospital.model.Medicine;
import com.hospital.model.OPDToken;
import com.hospital.model.OperationTheatre;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import com.hospital.model.Surgery;
import com.hospital.service.AdmissionService;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuditService;
import com.hospital.service.BedService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.InsuranceService;
import com.hospital.service.LabService;
import com.hospital.service.NotificationService;
import com.hospital.service.OPDService;
import com.hospital.service.PatientService;
import com.hospital.service.PharmacyService;
import com.hospital.service.SurgeryService;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final AuditService audit = new AuditService();
    private static final NotificationService notify = new NotificationService();
    private static final DoctorService doctorService = new DoctorService();
    private static final PatientService patientService = new PatientService(audit);
    private static final AppointmentService apptService = new AppointmentService(doctorService, patientService, notify);
    private static final OPDService opdService = new OPDService(patientService);
    private static final BedService bedService = new BedService();
    private static final AdmissionService admService = new AdmissionService(patientService, bedService, notify);
    private static final PharmacyService pharmacy = new PharmacyService();
    private static final LabService labService = new LabService(doctorService, notify);
    private static final SurgeryService surgeryService = new SurgeryService();
    private static final BillingService billingService = new BillingService(doctorService);
    private static final InsuranceService insuranceService = new InsuranceService();

    public static void main(String[] args) {
        printBanner();
        bootstrap();
        happyPath();

        boolean run = true;
        while (run) {
            printMenu();
            int c = readInt("Choose: ");
            System.out.println();
            try {
                switch (c) {
                    case 1: registerPatient();   break;
                    case 2: listPatients();      break;
                    case 3: bookAppointment();   break;
                    case 4: issueOPDToken();     break;
                    case 5: nextOPDToken();      break;
                    case 6: admitPatient();      break;
                    case 7: dischargePatient();  break;
                    case 8: sellMedicine();      break;
                    case 9: orderLabTest();      break;
                    case 10: scheduleSurgery();  break;
                    case 11: showBills();        break;
                    case 12: showReports();      break;
                    case 13: showAudit();        break;
                    case 14: run = false;        break;
                    default: System.out.println("Invalid.");
                }
            } catch (HospitalException ex) {
                System.out.println("BUSINESS ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("UNEXPECTED: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    private static void bootstrap() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 1 - Bootstrap demo data");
        System.out.println("=========================================================");

        Department dCard = doctorService.addDepartment(new Department("D1", "Cardiology"));
        Department dOrth = doctorService.addDepartment(new Department("D2", "Orthopedics"));
        doctorService.addDepartment(new Department("D3", "General"));

        Doctor dr1 = doctorService.add(new Doctor("DOC1", "Dr. Rao", dCard.getId(),
                LocalTime.of(9, 0), LocalTime.of(13, 0), 500));
        dr1.addSpecialization("Cardiologist");

        Doctor dr2 = doctorService.add(new Doctor("DOC2", "Dr. Sharma", dOrth.getId(),
                LocalTime.of(10, 0), LocalTime.of(14, 0), 400));
        dr2.addSpecialization("Orthopedic Surgeon");

        Doctor dr3 = doctorService.add(new Doctor("DOC3", "Dr. Kapoor", "D3",
                LocalTime.of(9, 0), LocalTime.of(17, 0), 300));
        dr3.addSpecialization("General Physician");

        // Beds
        bedService.addBed(new Bed("B1", BedType.GENERAL, "W1"));
        bedService.addBed(new Bed("B2", BedType.GENERAL, "W1"));
        bedService.addBed(new Bed("B3", BedType.PRIVATE, "W2"));
        bedService.addBed(new Bed("B4", BedType.ICU, "ICU"));
        bedService.addBed(new Bed("B5", BedType.EMERGENCY, "ER"));

        // Pharmacy
        pharmacy.add(new Medicine("M1", "Paracetamol", 20, LocalDate.now().plusYears(1), 100, false));
        pharmacy.add(new Medicine("M2", "Amoxicillin", 50, LocalDate.now().plusMonths(6), 30, true));
        pharmacy.add(new Medicine("M3", "Aspirin",     15, LocalDate.now().plusDays(10), 50, false));
        pharmacy.add(new Medicine("M4", "ExpiredMed",  10, LocalDate.now().minusDays(1), 20, false));

        // OT
        surgeryService.addTheatre(new OperationTheatre("OT1", "OT 1"));
        surgeryService.addTheatre(new OperationTheatre("OT2", "OT 2"));

        // Patients - 1 emergency, 2 regular
        Patient p1 = patientService.register("Alice",   30, Gender.FEMALE, "9000000001", "MG Road",   PatientType.REGULAR);
        Patient p2 = patientService.register("Bob",     65, Gender.MALE,   "9000000002", "Park St",   PatientType.REGULAR);
        Patient p3 = patientService.register("Charlie", 45, Gender.MALE,   null,         null,         PatientType.EMERGENCY);

        // Insurance
        insuranceService.add(new InsurancePolicy("INS1", p1.getId(), "StarHealth",
                LocalDate.now().plusYears(1), 50000));

        System.out.println("  3 doctors, 5 beds, 4 medicines, 2 OTs, 3 patients, 1 policy.");
        System.out.println();
    }

    private static void happyPath() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path automatic flow");
        System.out.println("=========================================================");
        try {
            // Alice books appointment
            Appointment apt = apptService.book("P1001", "DOC1",
                    LocalDate.now().atTime(10, 0), false);
            System.out.println("  Booked: " + apt);

            // OPD tokens - emergency first
            OPDToken t1 = opdService.issueToken("P1001", "DOC1");   // regular
            OPDToken t2 = opdService.issueToken("P1003", "DOC1");   // emergency!
            OPDToken t3 = opdService.issueToken("P1002", "DOC1");   // regular
            System.out.println("  Tokens issued in order: " + t1.getTokenNumber()
                    + ", " + t2.getTokenNumber() + ", " + t3.getTokenNumber());
            System.out.println("  Polling next (should be emergency first):");
            System.out.println("    " + opdService.pollNext());
            System.out.println("    " + opdService.pollNext());
            System.out.println("    " + opdService.pollNext());

            // Admit Bob to general bed
            Admission adm = admService.admit("P1002", BedType.GENERAL);
            System.out.println("  " + adm);

            // Build bill
            Bill bill = billingService.createBill("P1002");
            billingService.addConsultationCharge(bill.getId(), "DOC1");
            billingService.addRoomCharge(bill.getId(), 1000, 2);
            billingService.addLabCharge(bill.getId(), "Blood Test", 500);
            billingService.addPharmacyCharge(bill.getId(), "Paracetamol", 100);
            System.out.println("  " + bill);
            billingService.pay(bill.getId(), 0);
            System.out.println("  Bill paid.");

            // Discharge Bob
            admService.discharge(adm.getId());
        } catch (Exception ex) {
            System.out.println("  Demo flow error: " + ex.getMessage());
        }
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#             JHires Hospital Management System          #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. Register patient");
        System.out.println(" 2. List patients");
        System.out.println(" 3. Book appointment");
        System.out.println(" 4. Issue OPD token");
        System.out.println(" 5. Poll next OPD token (priority queue!)");
        System.out.println(" 6. Admit patient to bed");
        System.out.println(" 7. Discharge patient");
        System.out.println(" 8. Sell medicine");
        System.out.println(" 9. Order lab test");
        System.out.println("10. Schedule surgery");
        System.out.println("11. Show bills");
        System.out.println("12. Show reports");
        System.out.println("13. Show audit log");
        System.out.println("14. Exit");
        System.out.println("==============================================");
    }

    // ---------- Menu actions ----------
    private static void registerPatient() {
        String name = readString("Name : ");
        int age = readInt("Age : ");
        String g = readString("Gender (M/F/O): ").toUpperCase();
        Gender gender = g.startsWith("F") ? Gender.FEMALE
                : g.startsWith("O") ? Gender.OTHER : Gender.MALE;
        String mobile = readString("Mobile : ");
        String addr   = readString("Address: ");
        String t = readString("Type (R=regular, E=emergency): ").toUpperCase();
        PatientType type = t.startsWith("E") ? PatientType.EMERGENCY : PatientType.REGULAR;
        Patient p = patientService.register(name, age, gender,
                mobile.isBlank() ? null : mobile, addr.isBlank() ? null : addr, type);
        System.out.println("Registered: " + p);
    }

    private static void listPatients() {
        for (Patient p : patientService.getAll()) System.out.println(p);
    }

    private static void bookAppointment() {
        String pid = readString("Patient ID : ");
        String did = readString("Doctor ID  : ");
        int h = readInt("Hour (today) : ");
        int m = readInt("Minute       : ");
        Appointment a = apptService.book(pid, did, LocalDate.now().atTime(h, m), false);
        System.out.println("Booked: " + a);
    }

    private static void issueOPDToken() {
        String pid = readString("Patient ID : ");
        String did = readString("Doctor ID  : ");
        OPDToken t = opdService.issueToken(pid, did);
        System.out.println("Token: " + t);
    }

    private static void nextOPDToken() {
        OPDToken t = opdService.pollNext();
        if (t == null) System.out.println("Queue empty.");
        else { opdService.markInProgress(t); System.out.println("Now serving: " + t); }
    }

    private static void admitPatient() {
        String pid = readString("Patient ID : ");
        System.out.println("Bed type: 1.GENERAL 2.PRIVATE 3.ICU 4.EMERGENCY");
        int t = readInt("Choose : ");
        BedType type = t == 2 ? BedType.PRIVATE
                : t == 3 ? BedType.ICU
                : t == 4 ? BedType.EMERGENCY
                : BedType.GENERAL;
        Admission a = admService.admit(pid, type);
        System.out.println("Admitted: " + a);
    }

    private static void dischargePatient() {
        String aid = readString("Admission ID : ");
        admService.discharge(aid);
        System.out.println("Discharged.");
    }

    private static void sellMedicine() {
        String mid = readString("Medicine ID : ");
        int qty = readInt("Quantity     : ");
        double total = pharmacy.sell(mid, qty, null);
        System.out.println("Sold. Total Rs." + total);
    }

    private static void orderLabTest() {
        String pid = readString("Patient ID : ");
        String did = readString("Doctor ID  : ");
        String tests = readString("Tests CSV  : ");
        LabOrder o = labService.order(pid, did, Arrays.asList(tests.split(",")));
        System.out.println("Ordered: " + o);
    }

    private static void scheduleSurgery() {
        String pid = readString("Patient ID    : ");
        String otId = readString("OT ID         : ");
        String surgeons = readString("Surgeon IDs CSV: ");
        int hour = readInt("Start hour    : ");
        LocalDateTime start = LocalDateTime.now().withHour(hour).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        Surgery s = surgeryService.schedule(pid, otId,
                Arrays.asList(surgeons.split(",")), start, end);
        System.out.println("Scheduled: " + s);
    }

    private static void showBills() {
        for (Bill b : billingService.getAll()) {
            System.out.println(b);
            for (Bill.LineItem li : b.getItems()) System.out.println(li);
        }
    }

    private static void showReports() {
        System.out.println("--- Daily revenue ---");
        billingService.getDailyRevenue().forEach((k, v) -> System.out.println("  " + k + " Rs." + v));
        System.out.println("--- Insurance payments total ---");
        System.out.println("  Rs." + billingService.getInsurancePaymentsTotal());
    }

    private static void showAudit() {
        audit.getLogs().forEach(System.out::println);
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Invalid number."); }
        }
    }
}
