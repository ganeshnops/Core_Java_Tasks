package com.food.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.food.model.Customer;

public class CustomerService {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    public Customer add(Customer c) { customers.put(c.getId(), c); return c; }
    public Customer get(String id)  { return customers.get(id); }
    public Collection<Customer> getAll() { return Collections.unmodifiableCollection(customers.values()); }

    public void block(String id)   { Customer c = get(id); if (c != null) c.setBlocked(true); }
    public void unblock(String id) { Customer c = get(id); if (c != null) c.setBlocked(false); }
}
