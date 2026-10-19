package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import java.util.List;
import java.util.Set;

/**
 * TODO TID055: Exclude movies whose IDs appear in watch history before any scoring.
 */
public class WatchedMovieFilter {
    public List<Movie> filterUnwatched(List<Movie> movies, Set<String> watchedMovieIds) {
        throw new UnsupportedOperationException("TID055: WatchedMovieFilter is not implemented");
    }
}
