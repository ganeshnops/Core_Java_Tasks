package com.food.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.food.enums.OrderStatus;
import com.food.exception.FoodOrderException;
import com.food.model.DeliveryPartner;
import com.food.model.Order;
import com.food.model.Restaurant;
import com.food.model.Review;

/**
 * Reviews + rating recalculation.
 *  - Rule 39: only delivered orders can be rated.
 *  - Rule 40: one review per order.
 *  - Rule 41: restaurant rating recalculated.
 *  - Rule 42: delivery partner rating updated.
 */
public class ReviewService {

    private final List<Review> reviews = new CopyOnWriteArrayList<>();
    private final Set<String> reviewedOrderIds = ConcurrentHashMap.newKeySet();

    private final RestaurantService restaurantService;
    private final DeliveryService deliveryService;

    public ReviewService(RestaurantService restaurantService, DeliveryService deliveryService) {
        this.restaurantService = restaurantService;
        this.deliveryService = deliveryService;
    }

    public void addReview(Order order, int restaurantRating, int deliveryRating, String comment) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new FoodOrderException("Only delivered orders can be reviewed. Current: " + order.getStatus());
        }
        if (!reviewedOrderIds.add(order.getId())) {
            throw new FoodOrderException("Order already reviewed: " + order.getId());
        }
        if (restaurantRating < 1 || restaurantRating > 5 || deliveryRating < 1 || deliveryRating > 5) {
            throw new FoodOrderException("Ratings must be 1..5");
        }
        reviews.add(new Review(order.getId(), order.getCustomerId(),
                restaurantRating, deliveryRating, comment));

        // Rule 41 - recalculate restaurant rating
        Restaurant r = restaurantService.get(order.getRestaurantId());
        if (r != null) r.addRating(restaurantRating);

        // Rule 42 - update delivery partner rating
        if (order.getDeliveryPartnerId() != null) {
            DeliveryPartner dp = deliveryService.get(order.getDeliveryPartnerId());
            if (dp != null) dp.addRating(deliveryRating);
        }
    }

    public List<Review> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(reviews));
    }
}
