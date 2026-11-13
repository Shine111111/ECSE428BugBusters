package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
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

    /**
     * Sorts a subset of movies (e.g. current search results) using the repository.
     */
    public List<Movie> sortMovies(List<Movie> movies, String criterion, String direction) {
        if (movies == null || movies.isEmpty()) {
            throw new IllegalArgumentException("No results available to sort");
        }

        Sort sort = buildSort(criterion, direction);
        List<String> movieIds = movies.stream().map(Movie::getMovieId).toList();
        return movieRepository.findByMovieIdIn(movieIds, sort);
    }

    /**
     * Sorts the whole movie catalog using the repository.
     */
    public List<Movie> sortAllMovies(String criterion, String direction) {
        String property = toSortProperty(criterion);
        boolean descending = toSortDirection(direction) == Sort.Direction.DESC;

        return switch (property) {
            case "ratings" -> descending
                    ? movieRepository.findAllByOrderByRatingsDesc()
                    : movieRepository.findAllByOrderByRatingsAsc();
            case "startYear" -> descending
                    ? movieRepository.findAllByOrderByStartYearDesc()
                    : movieRepository.findAllByOrderByStartYearAsc();
            default -> movieRepository.findAll(buildSort(criterion, direction));
        };
    }

    private Sort buildSort(String criterion, String direction) {
        String property = toSortProperty(criterion);
        Sort.Direction sortDirection = toSortDirection(direction);

        Sort.Order primary = new Sort.Order(sortDirection, property);
        if ("title".equals(property)) {
            primary = primary.ignoreCase();
        }
        // Tie-break by title so ordering is deterministic
        return Sort.by(primary, Sort.Order.asc("title").ignoreCase());
    }

    private String toSortProperty(String criterion) {
        String normalized = criterion == null ? "" : criterion.trim().toLowerCase();
        return switch (normalized) {
            case "title", "name" -> "title";
            case "rating" -> "ratings";
            case "views" -> "numVotes";
            case "release_year", "year", "start_year" -> "startYear";
            default -> throw new IllegalArgumentException("Invalid sort criterion");
        };
    }

    private Sort.Direction toSortDirection(String direction) {
        String normalized = direction == null ? "" : direction.trim().toLowerCase();
        return switch (normalized) {
            case "ascending", "asc" -> Sort.Direction.ASC;
            case "descending", "desc" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("Invalid sort direction");
        };
    }
}