package com.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.booking.enums.ShowStatus;
import com.booking.exception.BookingException;
import com.booking.exception.ShowOverlapException;
import com.booking.model.Movie;
import com.booking.model.Show;

public class ShowService {

    private final Map<String, Show> shows = new ConcurrentHashMap<>();
    private final Object scheduleLock = new Object();

    private final MovieService movieService;

    public ShowService(MovieService movieService) {
        this.movieService = movieService;
    }

    public Show get(String id) { return shows.get(id); }
    public Collection<Show> getAll() { return Collections.unmodifiableCollection(shows.values()); }

    /**
     * Schedule a new show. Checks:
     *  - Movie exists and APPROVED (Movie rule 1).
     *  - Start before end (Show rule 2).
     *  - Doesn't overlap with another show on the same screen (Theater rule 5).
     */
    public Show schedule(Show show) {
        Movie m = movieService.get(show.getMovieId());
        if (m == null)           throw new BookingException("Movie not found: " + show.getMovieId());
        if (!m.isApproved())     throw new BookingException("Movie not approved: " + show.getMovieId());
        if (!show.getStartTime().isBefore(show.getEndTime())) {
            throw new BookingException("Show start must be before end.");
        }
        synchronized (scheduleLock) {
            for (Show s : shows.values()) {
                if (!s.getScreenId().equals(show.getScreenId())) continue;
                if (s.getStatus() == ShowStatus.CANCELLED || s.getStatus() == ShowStatus.COMPLETED) continue;
                if (s.overlapsWith(show.getStartTime(), show.getEndTime())) {
                    throw new ShowOverlapException("Show overlaps with " + s.getId() + " on same screen.");
                }
            }
            shows.put(show.getId(), show);
        }
        return show;
    }

    public void cancel(String showId) {
        Show s = shows.get(showId);
        if (s != null) s.setStatus(ShowStatus.CANCELLED);
    }

    /** Update status based on current time (could be called by a scheduler). */
    public void updateLifecycleStatus() {
        LocalDateTime now = LocalDateTime.now();
        for (Show s : shows.values()) {
            if (s.getStatus() == ShowStatus.SCHEDULED && !now.isBefore(s.getStartTime())) {
                s.setStatus(ShowStatus.RUNNING);
            }
            if (s.getStatus() == ShowStatus.RUNNING && now.isAfter(s.getEndTime())) {
                s.setStatus(ShowStatus.COMPLETED);
            }
        }
    }

    /** Find shows by screen (useful for admin views). */
    public List<Show> showsByScreen(String screenId) {
        List<Show> out = new ArrayList<>();
        for (Show s : shows.values()) if (s.getScreenId().equals(screenId)) out.add(s);
        return out;
    }
}
