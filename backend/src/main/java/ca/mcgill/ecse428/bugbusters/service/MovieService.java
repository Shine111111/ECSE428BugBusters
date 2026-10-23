package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import java.util.List;

public class MovieService {
    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    public List<Movie> searchMovies(String criterion, String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return repository.search(criterion, query);
    }

    public List<Movie> searchMoviesByTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return repository.findByTitle(title);
    }

    public List<Movie> searchMoviesByDirector(String director) {
        if (director == null || director.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return repository.findByDirector(director);
    }

    public List<Movie> searchMoviesByActor(String actor) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return repository.findByActor(actor);
    }

    public List<Movie> searchMoviesByGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return repository.findByGenre(genre);
    }

    public Movie findMovieById(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            throw new IllegalArgumentException("Movie ID cannot be empty");
        }
        return repository.findById(movieId);
    }
}