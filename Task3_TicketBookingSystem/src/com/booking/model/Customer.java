package com.booking.model;

/**
 * Customer / movie-goer.
 *  - Mobile and email must be verified to book.
 *  - id is unique per customer.
 */
public class Customer {

    private final String id;
    private final String name;
    private final String email;
    private final String mobile;
    private final boolean emailVerified;
    private final boolean mobileVerified;

    public Customer(String id, String name, String email, String mobile,
                    boolean emailVerified, boolean mobileVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.emailVerified = emailVerified;
        this.mobileVerified = mobileVerified;
    }

    public String getId()            { return id; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public String getMobile()        { return mobile; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean isMobileVerified(){ return mobileVerified; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | email-verified=%s | mobile-verified=%s",
                id, name, email, mobile, emailVerified, mobileVerified);
    }
}
