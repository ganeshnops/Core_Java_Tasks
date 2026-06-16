package com.ecommerce.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ecommerce.enums.CustomerStatus;
import com.ecommerce.exception.CustomerException;

/**
 * Customer entity.
 *  - Rule C1: unique customer ID
 *  - Rule C2: unique email
 *  - Rule C3: unique mobile
 *  - Rule C4: VERIFIED before ordering
 *  - Rule C5: BLOCKED cannot order
 *  - Rule C6: multiple addresses supported
 *  - Rule C7: default address maintained
 */
public class Customer {

    private final String id;
    private final String name;
    private final String email;
    private final String mobile;
    private volatile CustomerStatus status;
    private final List<Address> addresses = new CopyOnWriteArrayList<>();

    public Customer(String id, String name, String email, String mobile) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.status = CustomerStatus.UNVERIFIED;
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getEmail()           { return email; }
    public String getMobile()          { return mobile; }
    public CustomerStatus getStatus()  { return status; }
    public List<Address> getAddresses(){ return Collections.unmodifiableList(new ArrayList<>(addresses)); }

    public void setStatus(CustomerStatus s) { this.status = s; }

    public Address getDefaultAddress() {
        for (Address a : addresses) if (a.isDefault()) return a;
        return null;
    }

    public void addAddress(Address a) {
        if (a.isDefault()) {
            for (Address ex : addresses) ex.setDefault(false);
        } else if (addresses.isEmpty()) {
            a.setDefault(true);   // first address is default
        }
        addresses.add(a);
    }

    public void setDefaultAddress(String addressId) {
        Address found = null;
        for (Address a : addresses) {
            if (a.getId().equals(addressId)) found = a;
            else a.setDefault(false);
        }
        if (found == null) throw new CustomerException("Address not found: " + addressId);
        found.setDefault(true);
    }

    public boolean canOrder() {
        return status == CustomerStatus.VERIFIED && getDefaultAddress() != null;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %d addresses",
                id, name, email, mobile, status, addresses.size());
    }
}
