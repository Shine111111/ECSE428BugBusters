package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

    public List<Movie> searchMoviesByActor(String actorName) {
        return searchMovies("actor", actorName);
    }

    public List<Movie> searchMoviesByDirector(String directorName) {
        return searchMovies("director", directorName);
    }

    public Movie findMovieById(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            throw new IllegalArgumentException("Movie ID cannot be empty");
        }
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + movieId));
    }

    public List<Movie> sortMovies(List<Movie> movies, String criterion, String direction) {
        if (movies == null || movies.isEmpty()) {
            throw new IllegalArgumentException("No results available to sort");
        }

        String normalizedCriterion = criterion == null ? "" : criterion.trim().toLowerCase();
        String normalizedDirection = direction == null ? "" : direction.trim().toLowerCase();

        if (!isValidSortCriterion(normalizedCriterion)) {
            throw new IllegalArgumentException("Invalid sort criterion");
        }

        if (!"ascending".equals(normalizedDirection)
                && !"descending".equals(normalizedDirection)
                && !"asc".equals(normalizedDirection)
                && !"desc".equals(normalizedDirection)) {
            throw new IllegalArgumentException("Invalid sort direction");
        }

        Comparator<Movie> comparator = switch (normalizedCriterion) {
            case "title", "name" -> Comparator.comparing(
                    Movie::getTitle,
                    Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)
            );
            case "rating" -> Comparator.comparing(
                    Movie::getRatings,
                    Comparator.nullsLast(BigDecimal::compareTo)
            );
            case "views" -> Comparator.comparing(
                    movie -> movie.getNumVotes() == null ? 0L : movie.getNumVotes(),
                    Comparator.nullsLast(Long::compareTo)
            );
            case "release_year", "year", "start_year" -> Comparator.comparing(
                    Movie::getStartYear,
                    Comparator.nullsLast(Integer::compareTo)
            );
            default -> throw new IllegalArgumentException("Invalid sort criterion");
        };

        boolean descending = "descending".equals(normalizedDirection) || "desc".equals(normalizedDirection);
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort(
                descending
                        ? comparator.reversed().thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER)
                        : comparator.thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER)
        );
        return sorted;
    }

    private boolean isValidSortCriterion(String criterion) {
        return switch (criterion) {
            case "title", "name", "rating", "views", "release_year", "year", "start_year" -> true;
            default -> false;
        };
    }
}