package com.booking.model;

import java.time.LocalDateTime;

public class Review {

    private final String movieId;
    private final String customerId;
    private final int rating;        // 1-5
    private final String comment;
    private final LocalDateTime createdAt;

    public Review(String movieId, String customerId, int rating, String comment) {
        this.movieId = movieId;
        this.customerId = customerId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public String getMovieId()           { return movieId; }
    public String getCustomerId()        { return customerId; }
    public int getRating()               { return rating; }
    public String getComment()           { return comment; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] cust=%s rating=%d/5 \"%s\"",
                movieId, customerId, rating, comment);
    }
}
