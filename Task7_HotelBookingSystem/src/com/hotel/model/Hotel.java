package com.hotel.model;

import com.hotel.enums.HotelStatus;

/**
 * A hotel property. Main.java passes 4 args: (id, name, city, address).
 */
public class Hotel {

    private final String id;
    private final String name;
    private final String city;
    private final String address;
    private volatile HotelStatus status;

    public Hotel(String id, String name, String city, String address) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.status = HotelStatus.OPEN;
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getCity()            { return city; }
    public String getAddress()         { return address; }
    public HotelStatus getStatus()     { return status; }

    public void setStatus(HotelStatus s) { this.status = s; }
    public boolean isOpen() { return status == HotelStatus.OPEN; }

    @Override
    public String toString() {
        return id + " | " + name + " | " + city + " | " + address + " | " + status;
    }
}
