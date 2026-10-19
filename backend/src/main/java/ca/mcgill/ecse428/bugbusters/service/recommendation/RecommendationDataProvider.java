package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.model.UserPreferences;
import java.util.List;
import java.util.Set;

/**
 * Data access contract for recommendations while prerequisite services are unavailable.
 * A future implementation can use repositories or services, including the existing
 * ReviewService.getReviewsForUser, without changing the scoring components.
 */
public interface RecommendationDataProvider {
    // Include rated movies as well as candidates so rating-history comparisons are possible.
    List<Movie> getMovies();

    UserPreferences getPreferences(String userId);

    List<Review> getRatings(String userId);

    Set<String> getWatchedMovieIds(String userId);
}
