package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.model.UserPreferences;
import java.util.List;
import java.util.Set;

/**
 * Temporary hardcoded data source based on the feature's preference-ranking scenario.
 * All user IDs currently receive the same fixture: favorites but no ratings or watched
 * movies. Replace this provider with real data access when prerequisite functionality
 * is available. Other scenarios should supply their own fixtures in tests.
 */
public class StubRecommendationDataProvider implements RecommendationDataProvider {
    @Override
    public List<Movie> getMovies() {
        return List.of(
                new Movie("arrival", "Arrival", Set.of("Science Fiction", "Drama"),
                        Set.of("Amy Adams", "Jeremy Renner"), "Denis Villeneuve", 4.6, 900, 200, 4500),
                new Movie("blade-runner-2049", "Blade Runner 2049", Set.of("Science Fiction", "Drama"),
                        Set.of("Ryan Gosling", "Ana de Armas"), "Denis Villeneuve", 4.5, 850, 300, 5000),
                new Movie("la-la-land", "La La Land", Set.of("Musical", "Romance"),
                        Set.of("Ryan Gosling", "Emma Stone"), "Damien Chazelle", 4.2, 700, 100, 4200),
                new Movie("pride-and-prejudice", "Pride and Prejudice", Set.of("Romance", "Drama"),
                        Set.of("Keira Knightley"), "Joe Wright", 4.1, 700, 100, 3000),
                new Movie("the-dark-knight", "The Dark Knight", Set.of("Action", "Crime"),
                        Set.of("Christian Bale"), "Christopher Nolan", 4.8, 2000, 700, 9000));
    }

    @Override
    public UserPreferences getPreferences(String userId) {
        return new UserPreferences(Set.of("Science Fiction"),
                Set.of("Ryan Gosling"), Set.of("Denis Villeneuve"));
    }

    @Override
    public List<Review> getRatings(String userId) {
        return List.of();
    }

    @Override
    public Set<String> getWatchedMovieIds(String userId) {
        return Set.of();
    }
}
