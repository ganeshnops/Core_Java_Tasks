package com.hotel.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hotel.exception.HotelException;
import com.hotel.model.Hotel;

public class HotelService {

    private final Map<String, Hotel> hotels = new ConcurrentHashMap<>();

    public Hotel add(Hotel h) { hotels.put(h.getId(), h); return h; }

    public Hotel get(String id) {
        Hotel h = hotels.get(id);
        if (h == null) throw new HotelException("Hotel not found: " + id);
        return h;
    }

    public Collection<Hotel> getAll() { return Collections.unmodifiableCollection(hotels.values()); }
}
