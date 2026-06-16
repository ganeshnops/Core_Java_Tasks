package com.atm.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.atm.exception.ATMException;
import com.atm.model.Customer;

public class CustomerService {

    private final AtomicLong seq = new AtomicLong(1000);
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Set<String> usedMobiles = ConcurrentHashMap.newKeySet();

    public Customer register(String name, String mobile, String email) {
        if (!usedMobiles.add(mobile)) {
            throw new ATMException("Mobile already registered: " + mobile);
        }
        String id = "C" + seq.incrementAndGet();
        Customer c = new Customer(id, name, mobile, email);
        customers.put(id, c);
        return c;
    }

    public Customer get(String id) {
        Customer c = customers.get(id);
        if (c == null) throw new ATMException("Customer not found: " + id);
        return c;
    }

    public Collection<Customer> getAll() { return Collections.unmodifiableCollection(customers.values()); }
}
