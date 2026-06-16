package com.hotel.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hotel.enums.RoomStatus;
import com.hotel.exception.HotelException;
import com.hotel.model.Room;

public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room add(Room r) { rooms.put(r.getId(), r); return r; }

    public Room get(String id) {
        Room r = rooms.get(id);
        if (r == null) throw new HotelException("Room not found: " + id);
        return r;
    }

    public Collection<Room> getAll() { return Collections.unmodifiableCollection(rooms.values()); }

    public List<Room> byHotel(String hotelId) {
        List<Room> out = new ArrayList<>();
        for (Room r : rooms.values()) if (r.getHotelId().equals(hotelId)) out.add(r);
        return out;
    }

    public void setStatus(String id, RoomStatus s) { get(id).setStatus(s); }
}
