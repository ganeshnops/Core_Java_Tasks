package com.food.model;

import java.time.LocalDateTime;

/** Customer review of a delivered order (Rules 39-42). */
public class Review {

    private final String orderId;
    private final String customerId;
    private final int restaurantRating;       // 1-5
    private final int deliveryRating;         // 1-5
    private final String comment;
    private final LocalDateTime createdAt;

    public Review(String orderId, String customerId, int restaurantRating,
                  int deliveryRating, String comment) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantRating = restaurantRating;
        this.deliveryRating = deliveryRating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public String getOrderId()           { return orderId; }
    public String getCustomerId()        { return customerId; }
    public int getRestaurantRating()     { return restaurantRating; }
    public int getDeliveryRating()       { return deliveryRating; }
    public String getComment()           { return comment; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    @Override
    public String toString() {
        return String.format("Review[order=%s, cust=%s] rest=%d/5 delivery=%d/5 \"%s\"",
                orderId, customerId, restaurantRating, deliveryRating, comment);
    }
}
