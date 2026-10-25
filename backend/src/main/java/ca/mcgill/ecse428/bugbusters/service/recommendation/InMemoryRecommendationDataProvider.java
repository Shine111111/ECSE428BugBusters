package ca.mcgill.ecse428.bugbusters.service.recommendation;
import ca.mcgill.ecse428.bugbusters.model.*;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Application-owned catalog and user signals. Empty preferences/history are valid for new users. */
public class InMemoryRecommendationDataProvider implements RecommendationDataProvider {
    private final Map<String, Movie> movies = new ConcurrentHashMap<>();
    private final Map<String, UserPreferences> preferences = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> watched = new ConcurrentHashMap<>();
    private final ReviewService reviews;
    public InMemoryRecommendationDataProvider(ReviewService reviews) { this.reviews = Objects.requireNonNull(reviews); }
    public void saveMovie(Movie movie) { movies.put(movie.getId(), movie); }
    public void savePreferences(String userId, UserPreferences value) { preferences.put(userId, value); }
    public void saveWatchedMovies(String userId, Set<String> ids) { watched.put(userId, Set.copyOf(ids)); }
    public List<Movie> getMovies() { return List.copyOf(movies.values()); }
    public UserPreferences getPreferences(String userId) {
        return preferences.getOrDefault(userId, new UserPreferences(Set.of(), Set.of(), Set.of()));
    }
    public List<Review> getRatings(String userId) { return reviews.getReviewsForUser(userId); }
    public Set<String> getWatchedMovieIds(String userId) { return watched.getOrDefault(userId, Set.of()); }
}
