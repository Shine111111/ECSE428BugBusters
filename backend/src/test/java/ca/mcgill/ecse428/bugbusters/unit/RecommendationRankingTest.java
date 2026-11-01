
package ca.mcgill.ecse428.bugbusters.unit;

import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.service.recommendation.RecommendationRanker;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// These six tests cover the main ranking behavior, score precedence, negative scores, empty and single-item lists, and input preservation
class RecommendationRankingTest {

    private final RecommendationRanker ranker = new RecommendationRanker();

    private MovieRecommendationResponse recommendation(
            String title, double score, double rating, int views) {

        Movie movie = new Movie(
                title, title, Set.of("Drama"), Set.of(),
                "Director", rating, 0, 0, views);

        return new MovieRecommendationResponse(movie, score);
    }

    private List<String> titles(List<MovieRecommendationResponse> results) {
        return results.stream()
                .map(r -> r.getMovie().getTitle())
                .toList();
    }

    @Test
    void TID062_ranksByTotalScoreDescending() {
        var low = recommendation("Low", 10, 4.0, 100);
        var high = recommendation("High", 90, 4.0, 100);
        var medium = recommendation("Medium", 50, 4.0, 100);

        var ranked = ranker.rank(List.of(low, high, medium));

        assertEquals(List.of("High", "Medium", "Low"), titles(ranked));
    }

    @Test
    void TID062_scoreTakesPriorityOverRatingAndViews() {
        var highScore = recommendation("High Score", 50, 1.0, 1);
        var popular = recommendation("Popular", 49, 5.0, 100000);

        var ranked = ranker.rank(List.of(popular, highScore));

        assertEquals(List.of("High Score", "Popular"), titles(ranked));
    }

    @Test
    void TID062_supportsNegativeAndZeroScores() {
        var negative = recommendation("Negative", -20, 4.0, 100);
        var zero = recommendation("Zero", 0, 4.0, 100);
        var positive = recommendation("Positive", 10, 4.0, 100);

        var ranked = ranker.rank(List.of(negative, zero, positive));

        assertEquals(
                List.of("Positive", "Zero", "Negative"),
                titles(ranked));
    }

    @Test
    void TID062_returnsEmptyListForEmptyInput() {
        assertTrue(ranker.rank(List.of()).isEmpty());
    }

    @Test
    void TID062_singleRecommendationRemainsUnchanged() {
        var only = recommendation("Arrival", 25, 4.5, 1000);

        var ranked = ranker.rank(List.of(only));

        assertEquals(1, ranked.size());
        assertSame(only, ranked.get(0));
    }

    @Test
    void TID062_doesNotModifyInputList() {
        var first = recommendation("First", 10, 4.0, 100);
        var second = recommendation("Second", 20, 4.0, 100);
        var input = List.of(first, second);

        var ranked = ranker.rank(input);

        assertEquals(List.of(first, second), input);
        assertEquals(List.of(second, first), ranked);
    }
}
