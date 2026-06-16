package com.booking.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.booking.exception.BookingException;
import com.booking.model.Review;

public class ReviewService {

    private final List<Review> reviews = new CopyOnWriteArrayList<>();
    private final Set<String> reviewedBy = ConcurrentHashMap.newKeySet();

    private final java.util.Map<String, AtomicInteger> countByMovie = new ConcurrentHashMap<>();
    private final java.util.Map<String, AtomicLong> sumByMovieX100 = new ConcurrentHashMap<>();

    public void addReview(String movieId, String customerId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BookingException("Rating must be 1..5");
        }
        if (!reviewedBy.add(customerId + "|" + movieId)) {
            throw new BookingException("Customer already reviewed this movie.");
        }
        reviews.add(new Review(movieId, customerId, rating, comment));
        countByMovie.computeIfAbsent(movieId, k -> new AtomicInteger(0)).incrementAndGet();
        sumByMovieX100.computeIfAbsent(movieId, k -> new AtomicLong(0)).addAndGet(rating * 100L);
    }

    public double averageRating(String movieId) {
        AtomicInteger c = countByMovie.get(movieId);
        if (c == null || c.get() == 0) return 0;
        return sumByMovieX100.get(movieId).get() / 100.0 / c.get();
    }

    public List<Review> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(reviews));
    }
}
