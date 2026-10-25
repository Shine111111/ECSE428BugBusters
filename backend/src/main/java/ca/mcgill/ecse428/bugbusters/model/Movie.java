package ca.mcgill.ecse428.bugbusters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

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

    public Integer getNumVotes() {
        return numVotes;
    }

    public void setNumVotes(Integer numVotes) {
        this.numVotes = numVotes;
    }
}