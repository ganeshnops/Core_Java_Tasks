package com.food.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.food.enums.RestaurantStatus;
import com.food.model.MenuItem;
import com.food.model.Restaurant;

public class RestaurantService {

    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();

    public Restaurant addRestaurant(Restaurant r) {
        restaurants.put(r.getId(), r);
        return r;
    }

    public Restaurant get(String id)              { return restaurants.get(id); }
    public Collection<Restaurant> getAll()        { return Collections.unmodifiableCollection(restaurants.values()); }

    public void setStatus(String id, RestaurantStatus status) {
        Restaurant r = get(id);
        if (r != null) r.setStatus(status);
    }

    public void addMenuItem(String restaurantId, MenuItem item) {
        Restaurant r = get(restaurantId);
        if (r != null) r.addMenuItem(item);
    }
}
