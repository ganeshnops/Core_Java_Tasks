package com.ems.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ems.enums.UserRole;
import com.ems.exception.DuplicateException;
import com.ems.exception.NotFoundException;
import com.ems.exception.UnauthorizedException;
import com.ems.model.Employee;
import com.ems.model.User;

/**
 * Authentication + RBAC enforcement.
 *  - Unique username
 *  - Password hashing (SHA-256 + salt) in User
 *  - 3 failed logins -> account locked
 *  - RBAC checks:
 *      ADMIN/HR : can view all employees
 *      MANAGER  : can view direct/indirect reports
 *      EMPLOYEE : can view only own record
 */
public class AuthService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Set<String> usedUsernames = ConcurrentHashMap.newKeySet();

    private final EmployeeService employeeService;

    public AuthService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public User createUser(String username, String password, String employeeId, UserRole role) {
        if (!usedUsernames.add(username)) throw new DuplicateException("Username exists: " + username);
        User u = new User(username, password, employeeId, role);
        users.put(username, u);
        return u;
    }

    public User login(String username, String password) {
        User u = users.get(username);
        if (u == null) throw new UnauthorizedException("Invalid username or password.");
        if (u.isLocked()) throw new UnauthorizedException("Account LOCKED after too many failed attempts.");
        if (!u.verify(password)) {
            int remaining = User.MAX_FAILED_LOGINS - u.getFailedLogins();
            throw new UnauthorizedException("Wrong password. " + Math.max(0, remaining) + " attempts left.");
        }
        return u;
    }

    /** RBAC check - can {caller} view {targetEmployee}? */
    public void requireCanViewEmployee(User caller, String targetEmployeeId) {
        switch (caller.getRole()) {
            case ADMIN:
            case HR:
                return;   // can view all
            case MANAGER:
                if (caller.getEmployeeId() != null && caller.getEmployeeId().equals(targetEmployeeId)) return;
                if (caller.getEmployeeId() != null
                        && employeeService.isInReportingChain(targetEmployeeId, caller.getEmployeeId())) return;
                throw new UnauthorizedException("Manager can only view their reports.");
            case EMPLOYEE:
                if (!targetEmployeeId.equals(caller.getEmployeeId())) {
                    throw new UnauthorizedException("Employees can only view their own record.");
                }
                return;
        }
    }

    public User get(String username) {
        User u = users.get(username);
        if (u == null) throw new NotFoundException("User not found: " + username);
        return u;
    }

    public void unlock(String username) { get(username).unlock(); }
}
