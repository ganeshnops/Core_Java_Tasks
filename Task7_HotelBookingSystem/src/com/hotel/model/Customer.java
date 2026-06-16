package com.hotel.model;

public class Customer {

    private final String id;
    private final String name;
    private final String email;
    private final String mobile;
    private final boolean verified;

    public Customer(String id, String name, String email, String mobile, boolean verified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.verified = verified;
    }

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getMobile()      { return mobile; }
    public boolean isVerified()    { return verified; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | verified=%s", id, name, email, mobile, verified);
    }
}
