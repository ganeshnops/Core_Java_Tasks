package com.atm.model;

public class Customer {

    private final String id;
    private final String name;
    private final String mobile;
    private final String email;

    public Customer(String id, String name, String mobile, String email) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
    }

    public String getId()     { return id; }
    public String getName()   { return name; }
    public String getMobile() { return mobile; }
    public String getEmail()  { return email; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", id, name, mobile, email);
    }
}
