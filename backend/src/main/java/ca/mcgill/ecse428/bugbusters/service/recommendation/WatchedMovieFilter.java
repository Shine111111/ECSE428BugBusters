package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import java.util.List;
import java.util.Set;

/**
 * Excludes already watched movies by user from the recommended movies.
 * Filtering occurs before calculation of recommendation scoring.
 */
public class WatchedMovieFilter {
    public List<Movie> filterUnwatched(List<Movie> movies, Set<String> watchedMovieIds) {
        return movies.stream().filter(movie -> !watchedMovieIds.contains(movie.getId())).toList();
    }
}
