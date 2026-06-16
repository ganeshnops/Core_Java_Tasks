package com.hospital.model;

import java.time.LocalDate;

import com.hospital.enums.InsuranceClaimStatus;

public class InsurancePolicy {

    private final String id;
    private final String patientId;
    private final String provider;
    private final LocalDate validTill;
    private final double coverageLimit;
    private volatile InsuranceClaimStatus claimStatus;
    private volatile double approvedAmount;

    public InsurancePolicy(String id, String patientId, String provider,
                           LocalDate validTill, double coverageLimit) {
        this.id = id;
        this.patientId = patientId;
        this.provider = provider;
        this.validTill = validTill;
        this.coverageLimit = coverageLimit;
        this.claimStatus = InsuranceClaimStatus.PENDING;
    }

    public String getId()                          { return id; }
    public String getPatientId()                   { return patientId; }
    public String getProvider()                    { return provider; }
    public LocalDate getValidTill()                { return validTill; }
    public double getCoverageLimit()               { return coverageLimit; }
    public InsuranceClaimStatus getClaimStatus()   { return claimStatus; }
    public double getApprovedAmount()              { return approvedAmount; }

    public boolean isValid() { return !LocalDate.now().isAfter(validTill); }

    public void approve(double amount) {
        this.approvedAmount = Math.min(amount, coverageLimit);
        this.claimStatus = InsuranceClaimStatus.APPROVED;
    }
    public void reject() { this.claimStatus = InsuranceClaimStatus.REJECTED; }

    @Override
    public String toString() {
        return String.format("%s | patient=%s | %s | valid-till=%s | cover=Rs.%.2f | %s",
                id, patientId, provider, validTill, coverageLimit, claimStatus);
    }
}
