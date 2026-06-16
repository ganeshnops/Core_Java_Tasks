package com.ems.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.enums.CandidateStatus;
import com.ems.exception.DuplicateException;
import com.ems.exception.EMSException;
import com.ems.exception.NotFoundException;
import com.ems.model.Candidate;
import com.ems.model.Employee;

public class RecruitmentService {

    private final AtomicLong seq = new AtomicLong(6000);
    private final Map<String, Candidate> candidates = new ConcurrentHashMap<>();
    private final Set<String> usedEmails = ConcurrentHashMap.newKeySet();

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public RecruitmentService(EmployeeService es, DepartmentService ds) {
        this.employeeService = es;
        this.departmentService = ds;
    }

    public Candidate addCandidate(String name, String email, String mobile, String position) {
        if (!usedEmails.add(email)) throw new DuplicateException("Candidate email exists: " + email);
        String id = "CAND-" + seq.incrementAndGet();
        Candidate c = new Candidate(id, name, email, mobile, position);
        candidates.put(id, c);
        return c;
    }

    public Candidate get(String id) {
        Candidate c = candidates.get(id);
        if (c == null) throw new NotFoundException("Candidate not found: " + id);
        return c;
    }

    /** Schedule interview - check overlap with other candidates' interviews. */
    public void scheduleInterview(String candidateId, LocalDateTime time) {
        // Overlap check (1-hour window)
        LocalDateTime end = time.plusHours(1);
        for (Candidate other : candidates.values()) {
            if (other.getId().equals(candidateId)) continue;
            LocalDateTime ot = other.getInterviewTime();
            if (ot == null) continue;
            LocalDateTime oend = ot.plusHours(1);
            if (time.isBefore(oend) && ot.isBefore(end)) {
                throw new EMSException("Interview overlaps with " + other.getId() + " at " + ot);
            }
        }
        Candidate c = get(candidateId);
        c.setInterviewTime(time);
        c.setStatus(CandidateStatus.INTERVIEW_SCHEDULED);
    }

    public void markInterviewed(String candidateId) {
        get(candidateId).setStatus(CandidateStatus.INTERVIEWED);
    }

    public void offer(String candidateId) {
        get(candidateId).setStatus(CandidateStatus.OFFERED);
    }

    public void rejectCandidate(String candidateId) {
        get(candidateId).setStatus(CandidateStatus.REJECTED);
    }

    /** Convert OFFERED candidate to employee. */
    public Employee convertToEmployee(String candidateId, String departmentId, String designation) {
        Candidate c = get(candidateId);
        if (c.getStatus() != CandidateStatus.OFFERED) {
            throw new EMSException("Candidate must be OFFERED to convert. Current: " + c.getStatus());
        }
        departmentService.get(departmentId);
        Employee e = employeeService.register(c.getName(), c.getEmail(), c.getMobile(),
                LocalDate.now(), departmentId, designation);
        c.setStatus(CandidateStatus.JOINED);
        return e;
    }

    public Collection<Candidate> getAll() { return Collections.unmodifiableCollection(candidates.values()); }
}
