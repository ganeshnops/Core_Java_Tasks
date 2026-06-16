package com.ems.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.ems.enums.UserRole;
import com.ems.util.PasswordHasher;

public class User {

    public static final int MAX_FAILED_LOGINS = 3;

    private final String username;
    private volatile String passwordHash;
    private final String employeeId;       // null for ADMIN / HR pseudo-users
    private final UserRole role;
    private final AtomicInteger failedLogins = new AtomicInteger(0);
    private volatile boolean locked;

    public User(String username, String password, String employeeId, UserRole role) {
        this.username = username;
        this.passwordHash = PasswordHasher.hash(password);
        this.employeeId = employeeId;
        this.role = role;
    }

    public String getUsername()    { return username; }
    public String getEmployeeId()  { return employeeId; }
    public UserRole getRole()      { return role; }
    public boolean isLocked()      { return locked; }
    public int getFailedLogins()   { return failedLogins.get(); }

    public boolean verify(String password) {
        if (locked) return false;
        if (PasswordHasher.matches(password, passwordHash)) {
            failedLogins.set(0);
            return true;
        }
        if (failedLogins.incrementAndGet() >= MAX_FAILED_LOGINS) {
            locked = true;
        }
        return false;
    }

    public void unlock() {
        locked = false;
        failedLogins.set(0);
    }

    public void changePassword(String newPwd) {
        this.passwordHash = PasswordHasher.hash(newPwd);
    }

    @Override
    public String toString() {
        return String.format("%s | role=%s | emp=%s | locked=%s | failed=%d",
                username, role, employeeId == null ? "-" : employeeId, locked, failedLogins.get());
    }
}
