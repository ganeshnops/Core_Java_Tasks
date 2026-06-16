package com.booking.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.booking.enums.ShowStatus;

/**
 * One scheduled show of a movie on a screen.
 *  - status: SCHEDULED / RUNNING / CANCELLED / COMPLETED.
 *  - bookedSeats : seatId -> bookingId (the seats that are confirmed booked).
 *  - basePrice   : per-seat base price (multiplied by seat category multiplier
 *    and weekend / dynamic factors by PricingService).
 */
public class Show {

    private final String id;
    private final String movieId;
    private final String screenId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final double basePrice;
    private volatile ShowStatus status;

    /** seatId -> bookingId (confirmed bookings). Thread-safe map. */
    private final Map<String, String> bookedSeats = new ConcurrentHashMap<>();

    public Show(String id, String movieId, String screenId,
                LocalDateTime startTime, LocalDateTime endTime, double basePrice) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Show start must be before end.");
        }
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = ShowStatus.SCHEDULED;
    }

    public String getId()              { return id; }
    public String getMovieId()          { return movieId; }
    public String getScreenId()         { return screenId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime()   { return endTime; }
    public double getBasePrice()        { return basePrice; }
    public ShowStatus getStatus()       { return status; }
    public Map<String, String> getBookedSeats() { return bookedSeats; }

    public void setStatus(ShowStatus s) { this.status = s; }

    /** True if this show overlaps with another in time. */
    public boolean overlapsWith(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    public boolean canBeBookedNow() {
        return status == ShowStatus.SCHEDULED && LocalDateTime.now().isBefore(startTime);
    }

    @Override
    public String toString() {
        return String.format("%s | movie=%s | screen=%s | %s -> %s | basePrice=Rs.%.2f | %s | booked=%d",
                id, movieId, screenId, startTime, endTime, basePrice, status, bookedSeats.size());
    }
}
