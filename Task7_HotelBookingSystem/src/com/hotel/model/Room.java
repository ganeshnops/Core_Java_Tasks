package com.hotel.model;

import com.hotel.enums.RoomStatus;
import com.hotel.enums.RoomType;

/**
 * A bookable room inside a hotel.
 * Main.java constructs with: (id, hotelId, number, RoomType, pricePerNight, amenities)
 */
public class Room {

    private final String id;
    private final String hotelId;
    private final String roomNumber;
    private final RoomType type;
    private final double pricePerNight;     // effective nightly tariff (PricingService uses this)
    private final String amenities;
    private volatile RoomStatus status;

    public Room(String id, String hotelId, String roomNumber, RoomType type,
                double pricePerNight, String amenities) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.amenities = amenities;
        this.status = RoomStatus.AVAILABLE;
    }

    public String getId()             { return id; }
    public String getHotelId()        { return hotelId; }
    public String getRoomNumber()     { return roomNumber; }
    public RoomType getType()         { return type; }
    public double getPricePerNight()  { return pricePerNight; }
    public String getAmenities()      { return amenities; }
    public RoomStatus getStatus()     { return status; }

    public void setStatus(RoomStatus s) { this.status = s; }

    /** True when room is in AVAILABLE state — i.e. a new booking can take it. */
    public boolean isBookable() {
        return status == RoomStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("%s | hotel=%s | room#%s | %s | Rs.%.2f/night | %s | [%s]",
                id, hotelId, roomNumber, type, pricePerNight, status, amenities);
    }
}
