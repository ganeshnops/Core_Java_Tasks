package com.booking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.booking.enums.TheaterStatus;

public class Theater {

    private final String id;
    private final String name;
    private final String city;
    private volatile TheaterStatus status;
    private final List<Screen> screens = new ArrayList<>();

    public Theater(String id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.status = TheaterStatus.ACTIVE;
    }

    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getCity()         { return city; }
    public TheaterStatus getStatus(){ return status; }
    public List<Screen> getScreens(){ return Collections.unmodifiableList(screens); }

    public void setStatus(TheaterStatus s) { this.status = s; }
    public void addScreen(Screen s)        { screens.add(s); }

    public boolean isActive() { return status == TheaterStatus.ACTIVE; }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s - %d screens - %s",
                id, name, city, screens.size(), status);
    }
}
