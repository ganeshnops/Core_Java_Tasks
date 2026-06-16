package com.booking.model;

import com.booking.enums.SeatCategory;
import com.booking.enums.SeatStatus;

/**
 * Physical seat in a screen (not show-specific).
 *  - status: AVAILABLE / BLOCKED / DISABLED (admin can block/disable).
 *  - category: REGULAR / PREMIUM / RECLINER / VIP (different prices).
 */
public class Seat {

    private final String id;             // unique seat id (e.g., "S1-A1")
    private final String screenId;
    private final String seatNumber;     // e.g., "A1"
    private final SeatCategory category;
    private volatile SeatStatus status;

    public Seat(String id, String screenId, String seatNumber, SeatCategory category) {
        this.id = id;
        this.screenId = screenId;
        this.seatNumber = seatNumber;
        this.category = category;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getId()             { return id; }
    public String getScreenId()       { return screenId; }
    public String getSeatNumber()     { return seatNumber; }
    public SeatCategory getCategory() { return category; }
    public SeatStatus getStatus()     { return status; }

    public void setStatus(SeatStatus status) { this.status = status; }

    public boolean isBookable() { return status == SeatStatus.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s [%s]", seatNumber, id, category, status);
    }
}
