package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> searchMovies(String criterion, String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        String normalizedCriterion = criterion == null ? "" : criterion.trim().toLowerCase();

        return switch (normalizedCriterion) {
            case "title", "name" -> movieRepository.findByTitleContainingIgnoreCase(query);
            case "genre" -> movieRepository.findByGenreContainingIgnoreCase(query);
            case "actress", "actor", "cast" -> movieRepository.findMoviesByActorNameContainingIgnoreCase(query);
            case "director" -> movieRepository.findMoviesByDirectorNameContainingIgnoreCase(query);
            default -> movieRepository.searchMovies(query);
        };
    }

    public List<Movie> searchMoviesByTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> searchMoviesByGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return movieRepository.findByGenreContainingIgnoreCase(genre);
    }

    public List<Movie> searchMoviesByPerson(String personName) {
        if (personName == null || personName.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return movieRepository.findMoviesByPersonNameContainingIgnoreCase(personName);
    }

    public Movie findMovieById(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            throw new IllegalArgumentException("Movie ID cannot be empty");
        }
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + movieId));
    }
}