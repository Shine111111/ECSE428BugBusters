package ca.mcgill.ecse428.bugbusters.model;

import java.util.Set;

/**
 * Movie metadata needed for preference matching, rating-history comparisons,
 * popularity scoring, and deterministic ranking. IDs link movies to reviews and watch history.
 */
public class Movie {
    private final String id;
    private final String title;
    private final Set<String> genres;
    private final Set<String> actors;
    private final String director;
    private final double averageRating;
    private final int ratingCount;
    private final int commentCount;
    private final int viewCount;

    public Movie(String id, String title, Set<String> genres, Set<String> actors,
            String director, double averageRating, int ratingCount, int commentCount, int viewCount) {
        this.id = id;
        this.title = title;
        this.genres = Set.copyOf(genres);
        this.actors = Set.copyOf(actors);
        this.director = director;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public Set<String> getGenres() { return genres; }
    public Set<String> getActors() { return actors; }
    public String getDirector() { return director; }
    public double getAverageRating() { return averageRating; }
    public int getRatingCount() { return ratingCount; }
    public int getCommentCount() { return commentCount; }
    public int getViewCount() { return viewCount; }
}
