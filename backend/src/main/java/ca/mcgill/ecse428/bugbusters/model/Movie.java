package ca.mcgill.ecse428.bugbusters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import jakarta.persistence.Transient;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @Column(name = "movie_id")
    private String movieId;

    @Column(name = "title")
    private String title;

    @Column(name = "genre")
    private String genre;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "ratings")
    private BigDecimal ratings;

    @Column(name = "num_votes")
    private Long numVotes;

    // Cast and director metadata is not present in the catalog table.
    @Transient
    private Set<String> actors = Set.of();
    @Transient
    private String director;
    // Null means the popularity signal has not been collected yet.
    @Column(name = "comment_count")
    private Integer commentCount;
    @Column(name = "view_count")
    private Integer viewCount;

    public Movie(String id, String title, Set<String> genres, Set<String> actors,
                 String director, double averageRating, int ratingCount, int commentCount, int viewCount) {
        this(id, title, String.join(",", genres), null, null,
                BigDecimal.valueOf(averageRating), (long) ratingCount);
        this.actors = Set.copyOf(actors);
        this.director = director;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
    }

    public String getId() { return movieId; }
    public Set<String> getGenres() {
        if (genre == null || genre.isBlank()) return Set.of();
        return Arrays.stream(genre.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).collect(Collectors.toUnmodifiableSet());
    }
    public Set<String> getActors() { return actors; }
    public String getDirector() { return director; }
    public double getAverageRating() { return ratings == null ? 0 : ratings.doubleValue(); }
    public int getRatingCount() {
        return numVotes == null ? 0 : (int) Math.min(Integer.MAX_VALUE, numVotes);
    }
    public int getCommentCount() { return commentCount == null ? 0 : commentCount; }
    public int getViewCount() { return viewCount == null ? 0 : viewCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    /** Checks presence before trending uses the zero-default recommendation getters. */
    public boolean hasTrendingData() {
        return ratings != null && numVotes != null && commentCount != null && viewCount != null;
    }

    public Movie() {
    }

    public Movie(String movieId, String title, String genre, Integer startYear,
                Integer runtime, BigDecimal ratings, Long numVotes) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.startYear = startYear;
        this.runtime = runtime;
        this.ratings = ratings;
        this.numVotes = numVotes;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public BigDecimal getRatings() {
        return ratings;
    }

    public void setRatings(BigDecimal ratings) {
        this.ratings = ratings;
    }

    public Long getNumVotes() {
        return numVotes;
    }

    public void setNumVotes(Long numVotes) {
        this.numVotes = numVotes;
    }
}
