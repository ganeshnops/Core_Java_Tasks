package com.booking.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.booking.exception.InvalidCustomerException;
import com.booking.model.Customer;

public class CustomerService {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Set<String> usedEmails = ConcurrentHashMap.newKeySet();
    private final Set<String> usedMobiles = ConcurrentHashMap.newKeySet();

    public Customer add(Customer c) {
        // Customer rule - unique email and mobile
        if (!usedEmails.add(c.getEmail())) {
            throw new InvalidCustomerException("Email already registered: " + c.getEmail());
        }
        if (!usedMobiles.add(c.getMobile())) {
            throw new InvalidCustomerException("Mobile already registered: " + c.getMobile());
        }
        customers.put(c.getId(), c);
        return c;
    }

    public Customer get(String id) { return customers.get(id); }
    public Collection<Customer> getAll() { return Collections.unmodifiableCollection(customers.values()); }
}
