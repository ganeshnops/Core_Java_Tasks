package com.ems.model;

import java.time.LocalDate;

import com.ems.exception.EMSException;

public class PerformanceReview {

    private final String id;
    private final String employeeId;
    private final String reviewerId;     // manager ID
    private final String period;          // e.g., "2026-H1"
    private final int rating;             // 1-5
    private final String comments;
    private final LocalDate reviewDate;
    private volatile boolean completed;

    public PerformanceReview(String id, String employeeId, String reviewerId,
                             String period, int rating, String comments) {
        if (rating < 1 || rating > 5) throw new EMSException("Rating must be 1-5");
        this.id = id;
        this.employeeId = employeeId;
        this.reviewerId = reviewerId;
        this.period = period;
        this.rating = rating;
        this.comments = comments;
        this.reviewDate = LocalDate.now();
        this.completed = true;
    }

    public String getId()         { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getReviewerId() { return reviewerId; }
    public String getPeriod()     { return period; }
    public int getRating()        { return rating; }
    public String getComments()   { return comments; }
    public LocalDate getReviewDate() { return reviewDate; }
    public boolean isCompleted()  { return completed; }

    @Override
    public String toString() {
        return String.format("%s | emp=%s | reviewer=%s | %s | rating=%d/5 | %s",
                id, employeeId, reviewerId, period, rating, comments);
    }
}
