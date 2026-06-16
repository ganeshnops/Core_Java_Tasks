package com.ecommerce.model;

public class Address {

    private final String id;
    private final String customerId;
    private final String line1;
    private final String city;
    private final String pincode;
    private volatile boolean isDefault;

    public Address(String id, String customerId, String line1, String city, String pincode, boolean isDefault) {
        this.id = id;
        this.customerId = customerId;
        this.line1 = line1;
        this.city = city;
        this.pincode = pincode;
        this.isDefault = isDefault;
    }

    public String getId()         { return id; }
    public String getCustomerId() { return customerId; }
    public String getLine1()      { return line1; }
    public String getCity()       { return city; }
    public String getPincode()    { return pincode; }
    public boolean isDefault()    { return isDefault; }

    public void setDefault(boolean d) { this.isDefault = d; }

    @Override
    public String toString() {
        return String.format("%s | %s, %s - %s%s", id, line1, city, pincode, isDefault ? " (DEFAULT)" : "");
    }
}
