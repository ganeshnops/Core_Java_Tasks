package com.hotel.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hotel.exception.InvalidCustomerException;
import com.hotel.model.Customer;

public class CustomerService {

    private final AtomicLong seq = new AtomicLong(1000);
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Set<String> usedEmails = ConcurrentHashMap.newKeySet();
    private final Set<String> usedMobiles = ConcurrentHashMap.newKeySet();

    public Customer register(String name, String email, String mobile, boolean verified) {
        if (!usedEmails.add(email))  throw new InvalidCustomerException("Email used: " + email);
        if (!usedMobiles.add(mobile)) throw new InvalidCustomerException("Mobile used: " + mobile);
        String id = "C" + seq.incrementAndGet();
        Customer c = new Customer(id, name, email, mobile, verified);
        customers.put(id, c);
        return c;
    }

    public Customer get(String id) {
        Customer c = customers.get(id);
        if (c == null) throw new InvalidCustomerException("Customer not found: " + id);
        return c;
    }

    public Collection<Customer> getAll() { return Collections.unmodifiableCollection(customers.values()); }
}
