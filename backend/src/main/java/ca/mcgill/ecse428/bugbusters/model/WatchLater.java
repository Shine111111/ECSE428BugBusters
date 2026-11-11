package ca.mcgill.ecse428.bugbusters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

// One row per (user, movie) pair in a user's Watch Later playlist.
// The unique constraint stops the same movie from being saved twice for one user.
@Entity
@Table(name = "watch_later",
        uniqueConstraints = @UniqueConstraint(name = "uk_watch_later_user_movie", columnNames = {"user_id", "movie_id"}))
public class WatchLater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watch_later_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "movie_id", nullable = false)
    private String movieId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected WatchLater() {
    }

    public WatchLater(String userId, String movieId) {
        this(userId, movieId, Instant.now());
    }

    public WatchLater(String userId, String movieId, Instant addedAt) {
        this.userId = userId;
        this.movieId = movieId;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
