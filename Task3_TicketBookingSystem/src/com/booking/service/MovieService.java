package com.booking.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.booking.enums.MovieStatus;
import com.booking.model.Movie;

public class MovieService {

    private final Map<String, Movie> movies = new ConcurrentHashMap<>();

    public Movie add(Movie m) { movies.put(m.getId(), m); return m; }
    public Movie get(String id) { return movies.get(id); }
    public Collection<Movie> getAll() { return Collections.unmodifiableCollection(movies.values()); }

    public void approve(String movieId) {
        Movie m = movies.get(movieId);
        if (m != null) m.setStatus(MovieStatus.APPROVED);
    }
    public void reject(String movieId) {
        Movie m = movies.get(movieId);
        if (m != null) m.setStatus(MovieStatus.REJECTED);
    }
    public void expire(String movieId) {
        Movie m = movies.get(movieId);
        if (m != null) m.setStatus(MovieStatus.EXPIRED);
    }
}
