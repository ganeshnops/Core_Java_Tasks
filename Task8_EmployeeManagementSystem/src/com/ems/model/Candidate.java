package com.ems.model;

import java.time.LocalDateTime;

import com.ems.enums.CandidateStatus;

public class Candidate {

    private final String id;
    private final String name;
    private final String email;
    private final String mobile;
    private final String position;
    private volatile CandidateStatus status;
    private volatile LocalDateTime interviewTime;
    private final LocalDateTime appliedAt;

    public Candidate(String id, String name, String email, String mobile, String position) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.position = position;
        this.appliedAt = LocalDateTime.now();
        this.status = CandidateStatus.APPLIED;
    }

    public String getId()                  { return id; }
    public String getName()                { return name; }
    public String getEmail()               { return email; }
    public String getMobile()              { return mobile; }
    public String getPosition()            { return position; }
    public CandidateStatus getStatus()     { return status; }
    public LocalDateTime getInterviewTime(){ return interviewTime; }
    public LocalDateTime getAppliedAt()    { return appliedAt; }

    public void setStatus(CandidateStatus s)         { this.status = s; }
    public void setInterviewTime(LocalDateTime t)    { this.interviewTime = t; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s | applied=%s",
                id, name, email, mobile, position, status, appliedAt);
    }
}
