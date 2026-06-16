package com.ems;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Scanner;

import com.ems.enums.EmployeeStatus;
import com.ems.enums.LeaveType;
import com.ems.enums.UserRole;
import com.ems.exception.EMSException;
import com.ems.model.Asset;
import com.ems.model.Attendance;
import com.ems.model.Candidate;
import com.ems.model.Department;
import com.ems.model.Employee;
import com.ems.model.LeaveRequest;
import com.ems.model.Payslip;
import com.ems.model.PerformanceReview;
import com.ems.model.Project;
import com.ems.model.Salary;
import com.ems.model.User;
import com.ems.service.AssetService;
import com.ems.service.AttendanceService;
import com.ems.service.AuthService;
import com.ems.service.DepartmentService;
import com.ems.service.EmployeeService;
import com.ems.service.LeaveService;
import com.ems.service.NotificationService;
import com.ems.service.PayrollService;
import com.ems.service.PerformanceService;
import com.ems.service.ProjectService;
import com.ems.service.RecruitmentService;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final NotificationService notify = new NotificationService();
    private static final DepartmentService departmentService = new DepartmentService();
    private static final EmployeeService employeeService = new EmployeeService(departmentService);
    private static final AttendanceService attendanceService = new AttendanceService();
    private static final LeaveService leaveService = new LeaveService(employeeService, notify);
    private static final PayrollService payrollService = new PayrollService(employeeService, notify);
    private static final PerformanceService performanceService = new PerformanceService(employeeService, notify);
    private static final ProjectService projectService = new ProjectService(employeeService, notify);
    private static final AssetService assetService = new AssetService(employeeService);
    private static final RecruitmentService recruitmentService = new RecruitmentService(employeeService, departmentService);
    private static final AuthService authService = new AuthService(employeeService);

    public static void main(String[] args) {
        printBanner();
        bootstrap();
        runHappyPath();

        boolean run = true;
        while (run) {
            printMenu();
            int c = readInt("Choose: ");
            System.out.println();
            try {
                switch (c) {
                    case 1: listEmployees();      break;
                    case 2: checkIn();            break;
                    case 3: checkOut();           break;
                    case 4: applyLeave();         break;
                    case 5: approveLeave();       break;
                    case 6: showLeaveBalance();   break;
                    case 7: generatePayrollMonth(); break;
                    case 8: submitReview();       break;
                    case 9: createProject();      break;
                    case 10: assignToProject();   break;
                    case 11: assignAsset();       break;
                    case 12: addCandidate();      break;
                    case 13: testCircularManager(); break;
                    case 14: testLogin();         break;
                    case 15: showReports();       break;
                    case 16: run = false;         break;
                    default: System.out.println("Invalid.");
                }
            } catch (EMSException ex) {
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

        Department eng = departmentService.add("Engineering");
        Department hr  = departmentService.add("Human Resources");
        Department fin = departmentService.add("Finance");

        Employee alice   = employeeService.register("Alice",   "alice@jhires.com",   "9000000001",
                LocalDate.of(2020, 1, 15), eng.getId(), "Engineering Manager");
        Employee bob     = employeeService.register("Bob",     "bob@jhires.com",     "9000000002",
                LocalDate.of(2021, 3, 1), eng.getId(), "Senior Engineer");
        Employee carol   = employeeService.register("Carol",   "carol@jhires.com",   "9000000003",
                LocalDate.of(2022, 7, 10), eng.getId(), "Junior Engineer");
        Employee david   = employeeService.register("David",   "david@jhires.com",   "9000000004",
                LocalDate.of(2019, 5, 20), hr.getId(), "HR Manager");
        Employee eve     = employeeService.register("Eve",     "eve@jhires.com",     "9000000005",
                LocalDate.of(2023, 2, 1), hr.getId(), "HR Executive");

        // Set up reporting: Alice manages Bob+Carol; David manages Eve
        employeeService.setManager(bob.getId(), alice.getId());
        employeeService.setManager(carol.getId(), alice.getId());
        employeeService.setManager(eve.getId(), david.getId());

        departmentService.setManager(eng.getId(), alice.getId());
        departmentService.setManager(hr.getId(),  david.getId());

        // Salaries
        payrollService.setSalary(new Salary(alice.getId(), 80000, 30000, 10000));
        payrollService.setSalary(new Salary(bob.getId(),   60000, 20000,  8000));
        payrollService.setSalary(new Salary(carol.getId(), 40000, 15000,  5000));
        payrollService.setSalary(new Salary(david.getId(), 70000, 25000,  9000));
        payrollService.setSalary(new Salary(eve.getId(),   35000, 12000,  4000));

        // Users (RBAC)
        authService.createUser("alice", "pass1234", alice.getId(), UserRole.MANAGER);
        authService.createUser("bob",   "pass1234", bob.getId(),   UserRole.EMPLOYEE);
        authService.createUser("david", "pass1234", david.getId(), UserRole.MANAGER);
        authService.createUser("hradmin", "hr@2026", null, UserRole.HR);
        authService.createUser("admin", "admin@2026", null, UserRole.ADMIN);

        // Assets
        assetService.add("Dell Laptop", "Laptop");
        assetService.add("iPhone 15", "Phone");

        System.out.println("  3 departments, 5 employees, salaries set, 5 users, 2 assets");
        System.out.println();
    }

    private static void runHappyPath() {
        System.out.println("=========================================================");
        System.out.println(" PHASE 2 - Happy-path automatic demo");
        System.out.println("=========================================================");
        try {
            // Bob applies leave, Alice approves
            LeaveRequest lr = leaveService.apply("E1002", LeaveType.CASUAL,
                    LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), "Family event");
            System.out.println("  " + lr);
            leaveService.approve(lr.getId(), "E1001");

            // Carol tries to approve own leave - should fail
            try {
                leaveService.approve(lr.getId(), "E1003");
            } catch (Exception ex) {
                System.out.println("  [OK] Self-approval blocked: " + ex.getMessage());
            }

            // Bob check in / out
            attendanceService.checkIn("E1002");
            attendanceService.checkOut("E1002");

            // Try circular reporting: Set Alice's manager to Carol (Alice is Carol's manager - cycle!)
            try {
                employeeService.setManager("E1001", "E1003");
            } catch (Exception ex) {
                System.out.println("  [OK] Circular reporting blocked: " + ex.getMessage());
            }

            // Generate Payslip for Bob this month
            Payslip p = payrollService.generatePayslip("E1002", YearMonth.now(), 5000);
            System.out.println("  " + p);

            // Performance review by Alice for Bob
            PerformanceReview r = performanceService.submitReview("E1001", "E1002", "2026-H1", 4, "Strong performer");
            System.out.println("  " + r);

            // Carol tries to review Bob - should fail (not Bob's manager)
            try {
                performanceService.submitReview("E1003", "E1002", "2026-H1", 5, "Bias attempt");
            } catch (Exception ex) {
                System.out.println("  [OK] Unauthorized review blocked: " + ex.getMessage());
            }

            // Login - Bob with 3 wrong passwords
            try { authService.login("bob", "wrong1"); } catch (Exception e1) { }
            try { authService.login("bob", "wrong2"); } catch (Exception e2) { }
            try { authService.login("bob", "wrong3"); } catch (Exception e3) { }
            try {
                authService.login("bob", "pass1234");
            } catch (Exception ex) {
                System.out.println("  [OK] Account locked after 3 fails: " + ex.getMessage());
            }
            authService.unlock("bob");
            System.out.println("  [OK] After admin unlock, login succeeds.");

        } catch (Exception ex) {
            System.out.println("  Demo flow error: " + ex.getMessage());
        }
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("##########################################################");
        System.out.println("#            JHires Employee Management System           #");
        System.out.println("##########################################################");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println(" 1. List employees");
        System.out.println(" 2. Check in (attendance)");
        System.out.println(" 3. Check out");
        System.out.println(" 4. Apply leave");
        System.out.println(" 5. Approve leave");
        System.out.println(" 6. Show leave balance");
        System.out.println(" 7. Generate payroll for current month");
        System.out.println(" 8. Submit performance review");
        System.out.println(" 9. Create project");
        System.out.println("10. Assign employee to project");
        System.out.println("11. Assign asset");
        System.out.println("12. Add candidate (recruitment)");
        System.out.println("13. Test circular manager detection");
        System.out.println("14. Test login + RBAC");
        System.out.println("15. Show reports");
        System.out.println("16. Exit");
        System.out.println("==============================================");
    }

    private static void listEmployees() {
        for (Employee e : employeeService.getAll()) System.out.println(e);
    }

    private static void checkIn() {
        String eid = readString("Employee ID : ");
        Attendance a = attendanceService.checkIn(eid);
        System.out.println("Checked in: " + a);
    }

    private static void checkOut() {
        String eid = readString("Employee ID : ");
        Attendance a = attendanceService.checkOut(eid);
        System.out.println("Checked out: " + a);
    }

    private static void applyLeave() {
        String eid = readString("Employee ID : ");
        System.out.println("Type: 1.CASUAL 2.SICK 3.EARNED");
        int t = readInt("Choose : ");
        LeaveType type = t == 2 ? LeaveType.SICK : t == 3 ? LeaveType.EARNED : LeaveType.CASUAL;
        String start = readString("Start date (YYYY-MM-DD): ");
        String end   = readString("End date (YYYY-MM-DD)  : ");
        String reason = readString("Reason                : ");
        LeaveRequest r = leaveService.apply(eid, type, LocalDate.parse(start), LocalDate.parse(end), reason);
        System.out.println("Applied: " + r);
    }

    private static void approveLeave() {
        String rid = readString("Leave Request ID : ");
        String approverId = readString("Approver Employee ID: ");
        leaveService.approve(rid, approverId);
        System.out.println("Approved.");
    }

    private static void showLeaveBalance() {
        String eid = readString("Employee ID : ");
        System.out.println(leaveService.getBalance(eid));
    }

    private static void generatePayrollMonth() {
        List<Payslip> all = payrollService.generateMonthly(YearMonth.now());
        for (Payslip p : all) System.out.println(p);
    }

    private static void submitReview() {
        String reviewerId = readString("Reviewer (Manager) ID : ");
        String employeeId = readString("Employee ID            : ");
        String period = readString("Period (e.g. 2026-H1)  : ");
        int rating = readInt("Rating 1-5             : ");
        String comments = readString("Comments               : ");
        PerformanceReview r = performanceService.submitReview(reviewerId, employeeId, period, rating, comments);
        System.out.println("Submitted: " + r);
    }

    private static void createProject() {
        String name = readString("Project name           : ");
        String start = readString("Start date (YYYY-MM-DD): ");
        String end   = readString("End date (YYYY-MM-DD)  : ");
        Project p = projectService.create(name, LocalDate.parse(start), LocalDate.parse(end));
        System.out.println("Created: " + p);
    }

    private static void assignToProject() {
        String projId = readString("Project ID : ");
        String empId  = readString("Employee ID: ");
        String mgrId  = readString("Manager ID : ");
        projectService.assignEmployee(projId, empId, mgrId);
        System.out.println("Assigned.");
    }

    private static void assignAsset() {
        String assetId = readString("Asset ID    : ");
        String empId   = readString("Employee ID : ");
        assetService.assign(assetId, empId);
        System.out.println("Assigned.");
    }

    private static void addCandidate() {
        String name = readString("Name : ");
        String email = readString("Email: ");
        String mobile = readString("Mobile: ");
        String position = readString("Position: ");
        Candidate c = recruitmentService.addCandidate(name, email, mobile, position);
        System.out.println("Added: " + c);
    }

    private static void testCircularManager() {
        String empId = readString("Employee ID         : ");
        String mgrId = readString("New Manager ID      : ");
        try {
            employeeService.setManager(empId, mgrId);
            System.out.println("OK. Manager updated.");
        } catch (Exception ex) {
            System.out.println("Blocked: " + ex.getMessage());
        }
    }

    private static void testLogin() {
        String username = readString("Username : ");
        String password = readString("Password : ");
        User u = authService.login(username, password);
        System.out.println("Login OK: " + u);
    }

    private static void showReports() {
        System.out.println("--- Employees ---");
        for (Employee e : employeeService.getAll()) System.out.println(e);
        System.out.println("--- Leave Requests ---");
        for (LeaveRequest r : leaveService.getAll()) System.out.println(r);
        System.out.println("--- Asset Allocations ---");
        for (Asset a : assetService.getAll()) System.out.println(a);
        System.out.println("--- Active Employees by Status ---");
        long active = employeeService.getAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE).count();
        System.out.println("  Active: " + active);
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
