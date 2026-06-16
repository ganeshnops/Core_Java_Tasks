package com.booking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.booking.enums.ScreenStatus;

/**
 * Screen inside a theater. Has a fixed list of physical seats.
 *  - status: ACTIVE / MAINTENANCE (Rule 7 - screens can be disabled temporarily).
 */
public class Screen {

    private final String id;
    private final String theaterId;
    private final String name;
    private volatile ScreenStatus status;
    private final List<Seat> seats = new ArrayList<>();

    public Screen(String id, String theaterId, String name) {
        this.id = id;
        this.theaterId = theaterId;
        this.name = name;
        this.status = ScreenStatus.ACTIVE;
    }

    public String getId()           { return id; }
    public String getTheaterId()    { return theaterId; }
    public String getName()         { return name; }
    public ScreenStatus getStatus() { return status; }
    public List<Seat> getSeats()    { return Collections.unmodifiableList(seats); }
    public int getCapacity()        { return seats.size(); }

    public void setStatus(ScreenStatus s) { this.status = s; }
    public void addSeat(Seat s)            { seats.add(s); }

    public boolean isActive() { return status == ScreenStatus.ACTIVE; }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s - %d seats - %s",
                id, name, theaterId, seats.size(), status);
    }
}
