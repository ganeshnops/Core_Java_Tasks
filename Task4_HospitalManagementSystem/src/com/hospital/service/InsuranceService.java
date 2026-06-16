package com.hospital.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hospital.exception.HospitalException;
import com.hospital.exception.NotFoundException;
import com.hospital.model.InsurancePolicy;

public class InsuranceService {

    private final Map<String, InsurancePolicy> policies = new ConcurrentHashMap<>();

    public InsurancePolicy add(InsurancePolicy p) { policies.put(p.getId(), p); return p; }
    public InsurancePolicy get(String id) {
        InsurancePolicy p = policies.get(id);
        if (p == null) throw new NotFoundException("Policy not found: " + id);
        return p;
    }

    /** Validate + approve a claim of amount. Returns approved amount. */
    public double processClaim(String policyId, double claimAmount) {
        InsurancePolicy p = get(policyId);
        if (!p.isValid()) {
            p.reject();
            throw new HospitalException("Insurance policy expired: " + policyId);
        }
        p.approve(claimAmount);
        return p.getApprovedAmount();
    }
}
