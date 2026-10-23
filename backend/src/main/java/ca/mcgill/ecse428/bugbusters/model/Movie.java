package ca.mcgill.ecse428.bugbusters.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Movie {
    private final String movieId;
    private final String title;
    private final String director;
    private final List<String> actors;
    private final String genre;

    public Movie(String movieId, String title, String director, List<String> actors, String genre) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.actors = actors == null ? new ArrayList<>() : new ArrayList<>(actors);
        this.genre = genre;
    }

    public Movie(String movieId, String title, String director, String[] actors, String genre) {
        this(movieId, title, director,
                actors == null ? Collections.emptyList() : List.of(actors),
                genre);
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public List<String> getActors() {
        return new ArrayList<>(actors);
    }

    public String getGenre() {
        return genre;
    }
}