package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieRepository {
    private final Map<String, Movie> moviesById = new HashMap<>();

    public void save(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        if (movie.getMovieId() == null || movie.getMovieId().isBlank()) {
            throw new IllegalArgumentException("Movie ID cannot be null or blank");
        }
        moviesById.put(movie.getMovieId(), movie);
    }

    public Movie findById(String movieId) {
        return moviesById.get(movieId);
    }

    public List<Movie> getAll() {
        return new ArrayList<>(moviesById.values());
    }

    public List<Movie> findByTitle(String title) { // TID040 - Lucia
        return filterByQuery(title, Movie::getTitle);
        
    }

    public List<Movie> findByDirector(String director) { // TID043 - Shayan
        return filterByQuery(director, Movie::getDirector);
    }

    public List<Movie> findByActor(String actor) { // TID041 - Shayan
        List<Movie> result = new ArrayList<>();
        for (Movie movie : moviesById.values()) {
            for (String a : movie.getActors()) {
                if (matches(a, actor)) {
                    result.add(movie);
                    break;
                }
            }
        }
        return result;
    }

    public List<Movie> findByGenre(String genre) { // TID044 - Shayan
        return filterByQuery(genre, Movie::getGenre);
    }

    public List<Movie> search(String criterion, String query) {
        if (criterion == null || criterion.isBlank()) {
            throw new IllegalArgumentException("Search criterion cannot be empty");
        }

        String normalizedCriterion = criterion.trim().toLowerCase(Locale.ROOT);
        switch (normalizedCriterion) {
            case "title":
            case "name":
                return findByTitle(query);
            case "director":
                return findByDirector(query);
            case "actor":
                return findByActor(query);
            case "genre":
                return findByGenre(query);
            default:
                throw new IllegalArgumentException("Unsupported search criterion: " + criterion);
        }
    }

    private List<Movie> filterByQuery(String query, java.util.function.Function<Movie, String> extractor) {
        List<Movie> result = new ArrayList<>();
        for (Movie movie : moviesById.values()) {
            if (matches(extractor.apply(movie), query)) {
                result.add(movie);
            }
        }
        return result;
    }

    private boolean matches(String fieldValue, String query) {
        if (fieldValue == null || query == null) {
            return false;
        }
        String normalizedField = fieldValue.trim().toLowerCase(Locale.ROOT);
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return !normalizedQuery.isEmpty() && normalizedField.contains(normalizedQuery);
    }
}