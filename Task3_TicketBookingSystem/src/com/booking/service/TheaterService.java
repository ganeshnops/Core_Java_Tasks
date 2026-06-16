package com.booking.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.booking.enums.ScreenStatus;
import com.booking.enums.SeatStatus;
import com.booking.model.Screen;
import com.booking.model.Seat;
import com.booking.model.Theater;

public class TheaterService {

    private final Map<String, Theater> theaters = new ConcurrentHashMap<>();
    private final Map<String, Screen> screens = new ConcurrentHashMap<>();
    private final Map<String, Seat> seats = new ConcurrentHashMap<>();

    public Theater addTheater(Theater t) {
        theaters.put(t.getId(), t);
        return t;
    }

    public void addScreen(Screen s) {
        screens.put(s.getId(), s);
        Theater t = theaters.get(s.getTheaterId());
        if (t != null) t.addScreen(s);
    }

    public void addSeat(Seat s) {
        seats.put(s.getId(), s);
        Screen sc = screens.get(s.getScreenId());
        if (sc != null) sc.addSeat(s);
    }

    public Theater getTheater(String id)         { return theaters.get(id); }
    public Screen getScreen(String id)           { return screens.get(id); }
    public Seat getSeat(String id)               { return seats.get(id); }
    public Collection<Theater> getAllTheaters()  { return Collections.unmodifiableCollection(theaters.values()); }

    /** Admin operations (Rule: Admin can block seats / disable screens / maintain) */
    public void blockSeat(String seatId)           { Seat s = seats.get(seatId); if (s != null) s.setStatus(SeatStatus.BLOCKED); }
    public void unblockSeat(String seatId)         { Seat s = seats.get(seatId); if (s != null) s.setStatus(SeatStatus.AVAILABLE); }
    public void disableSeat(String seatId)         { Seat s = seats.get(seatId); if (s != null) s.setStatus(SeatStatus.DISABLED); }
    public void setScreenStatus(String screenId, ScreenStatus st) {
        Screen sc = screens.get(screenId);
        if (sc != null) sc.setStatus(st);
    }
}
