package ca.mcgill.ecse428.bugbusters.unit;
import ca.mcgill.ecse428.bugbusters.model.*;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.*;
import ca.mcgill.ecse428.bugbusters.service.recommendation.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationTest {
    private final ReviewService reviews = new ReviewService(new ReviewRepository());
    private final InMemoryRecommendationDataProvider data = new InMemoryRecommendationDataProvider(reviews);
    private final RecommendationService service = new RecommendationService(data, new WatchedMovieFilter(), new PreferenceScorer(),
        new RatingHistoryScorer(), new PopularityScorer(), new RecommendationRanker());
    private Movie movie(String id) { return new Movie(id, id, Set.of("Drama", "Romance"), Set.of("A", "B"), "D", 4.6, 900, 200, 100); }
    @Test void filtersWatchedByIdAndPreservesInput() {
        var movies = List.of(movie("one"), movie("two"));
        assertEquals(List.of(movies.get(1)), new WatchedMovieFilter().filterUnwatched(movies, Set.of("one", "unknown")));
        assertEquals(2, movies.size());
    }
    @Test void filteringHandlesEmptyAndAllWatched() {
        var filter = new WatchedMovieFilter(); var movies = List.of(movie("one"));
        assertEquals(movies, filter.filterUnwatched(movies, Set.of()));
        assertTrue(filter.filterUnwatched(movies, Set.of("one")).isEmpty());
        assertTrue(filter.filterUnwatched(List.of(), Set.of("one")).isEmpty());
    }
    @Test void emptyCatalogAndFullyWatchedCatalogReturnEmpty() {
        assertTrue(service.getRecommendations("u").isEmpty());
        data.saveMovie(movie("one")); data.saveWatchedMovies("u", Set.of("one"));
        assertTrue(service.getRecommendations("u").isEmpty());
    }
    @Test void newUserGetsPopularityAndUserSignalsAreIsolated() {
        data.saveMovie(movie("one")); data.saveWatchedMovies("other", Set.of("one"));
        data.savePreferences("other", new UserPreferences(Set.of("Drama"), Set.of(), Set.of()));
        assertEquals(20.2, service.getRecommendations("u").get(0).getTotalScore(), 0.00001);
        assertTrue(service.getRecommendations("other").isEmpty());
    }
    @Test void ratedMoviesAreExcludedEvenWithoutSeparateWatchHistory() {
        data.saveMovie(movie("one")); reviews.storeReview(new Review("u", "one", 5, null));
        assertTrue(service.getRecommendations("u").isEmpty());
    }
    @Test void rejectsMissingUserId() {
        assertThrows(IllegalArgumentException.class, () -> service.getRecommendations(null));
        assertThrows(IllegalArgumentException.class, () -> service.getRecommendations("  "));
    }
    @Test void preferencesCountEveryMatchingFavorite() {
        assertEquals(135, new PreferenceScorer().score(movie("one"), new UserPreferences(Set.of("Drama", "Romance"), Set.of("A", "B"), Set.of("D"))));
    }
    @Test void popularityCapsCountsAndPreservesFractions() {
        assertEquals(24.6, new PopularityScorer().score(new Movie("a", "a", Set.of(), Set.of(), "d", 4.8, 2000, 700, 0)), 0.00001);
        assertEquals(20.2, new PopularityScorer().score(movie("a")), 0.00001);
    }
    @Test void historyCountsCategoriesOnceAndHandlesNeutralAndMissingMetadata() {
        var scorer = new RatingHistoryScorer(); var candidate = movie("a"); var catalog = Map.of("b", movie("b"));
        assertEquals(35, scorer.score(candidate, List.of(new Review("u", "b", 5, null)), catalog));
        assertEquals(-50, scorer.score(candidate, List.of(new Review("u", "b", 1, null)), catalog));
        assertEquals(0, scorer.score(candidate, List.of(new Review("u", "b", 3, null), new Review("u", "missing", 5, null)), catalog));
        assertThrows(IllegalArgumentException.class, () -> scorer.score(candidate, List.of(new Review("u", "b", 6, null)), catalog));
    }
}
