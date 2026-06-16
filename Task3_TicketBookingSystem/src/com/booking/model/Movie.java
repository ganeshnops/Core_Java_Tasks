package com.booking.model;

import com.booking.enums.MovieStatus;

/**
 * Movie metadata.
 *  - approval flow: PENDING_APPROVAL -> APPROVED / REJECTED.
 *  - has duration, language, genre, rating.
 *  - same movie can be in many theaters (Movie rule 5) - just reference movieId.
 */
public class Movie {

    private final String id;
    private final String title;
    private final int durationMin;
    private final String language;
    private final String genre;
    private final String rating;          // e.g., U, U/A, A
    private volatile MovieStatus status;

    public Movie(String id, String title, int durationMin,
                 String language, String genre, String rating) {
        this.id = id;
        this.title = title;
        this.durationMin = durationMin;
        this.language = language;
        this.genre = genre;
        this.rating = rating;
        this.status = MovieStatus.PENDING_APPROVAL;
    }

    public String getId()            { return id; }
    public String getTitle()         { return title; }
    public int getDurationMin()      { return durationMin; }
    public String getLanguage()      { return language; }
    public String getGenre()         { return genre; }
    public String getRating()        { return rating; }
    public MovieStatus getStatus()   { return status; }

    public void setStatus(MovieStatus s) { this.status = s; }

    public boolean isApproved() { return status == MovieStatus.APPROVED; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %d min | %s | %s",
                id, title, language, genre, durationMin, rating, status);
    }
}
