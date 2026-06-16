package com.ecommerce.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ecommerce.enums.CustomerStatus;
import com.ecommerce.exception.CustomerException;
import com.ecommerce.model.Address;
import com.ecommerce.model.Customer;

public class CustomerService {

    private final AtomicLong custSeq = new AtomicLong(1000);
    private final AtomicLong addrSeq = new AtomicLong(5000);
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Set<String> usedEmails = ConcurrentHashMap.newKeySet();
    private final Set<String> usedMobiles = ConcurrentHashMap.newKeySet();

    public Customer register(String name, String email, String mobile) {
        if (!usedEmails.add(email)) throw new CustomerException("Email already in use: " + email);
        if (!usedMobiles.add(mobile)) throw new CustomerException("Mobile already in use: " + mobile);
        String id = "C" + custSeq.incrementAndGet();
        Customer c = new Customer(id, name, email, mobile);
        customers.put(id, c);
        return c;
    }

    public Customer get(String id) {
        Customer c = customers.get(id);
        if (c == null) throw new CustomerException("Customer not found: " + id);
        return c;
    }

    public Collection<Customer> getAll() { return Collections.unmodifiableCollection(customers.values()); }

    public void verify(String id)  { get(id).setStatus(CustomerStatus.VERIFIED); }
    public void block(String id)   { get(id).setStatus(CustomerStatus.BLOCKED); }
    public void unblock(String id) { get(id).setStatus(CustomerStatus.VERIFIED); }

    public Address addAddress(String customerId, String line1, String city, String pincode, boolean isDefault) {
        Customer c = get(customerId);
        String addrId = "A" + addrSeq.incrementAndGet();
        Address a = new Address(addrId, customerId, line1, city, pincode, isDefault);
        c.addAddress(a);
        return a;
    }
}
