
package ca.mcgill.ecse428.bugbusters.unit;

import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.service.recommendation.RecommendationRanker;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// This adds seven dedicated tie-breaking tests, including the exact four-movie ordering from your Gherkin feature.
class RecommendationTieBreakingTest {

    private final RecommendationRanker ranker = new RecommendationRanker();

    private MovieRecommendationResponse recommendation(
            String title, double score, double rating, int views) {

        Movie movie = new Movie(
                title, title, Set.of("Drama"), Set.of(),
                "Director", rating, 0, 0, views);

        return new MovieRecommendationResponse(movie, score);
    }

    private List<String> titles(MovieRecommendationResponse... items) {
        return ranker.rank(List.of(items)).stream()
                .map(r -> r.getMovie().getTitle())
                .toList();
    }

    @Test
    void TID063_higherAverageRatingWinsScoreTie() {
        var lower = recommendation("Lower Rating", 50, 4.0, 10000);
        var higher = recommendation("Higher Rating", 50, 4.8, 100);

        assertEquals(
                List.of("Higher Rating", "Lower Rating"),
                titles(lower, higher));
    }

    @Test
    void TID063_higherViewCountWinsScoreAndRatingTie() {
        var fewer = recommendation("Fewer Views", 50, 4.5, 100);
        var more = recommendation("More Views", 50, 4.5, 5000);

        assertEquals(
                List.of("More Views", "Fewer Views"),
                titles(fewer, more));
    }

    @Test
    void TID063_alphabeticalTitleWinsCompleteNumericTie() {
        var zulu = recommendation("Zulu", 50, 4.5, 1000);
        var alpha = recommendation("Alpha", 50, 4.5, 1000);

        assertEquals(List.of("Alpha", "Zulu"), titles(zulu, alpha));
    }

    @Test
    void TID063_ratingTakesPriorityOverViewsAndTitle() {
        var higherRating = recommendation("Zulu", 50, 4.8, 10);
        var higherViews = recommendation("Alpha", 50, 4.5, 10000);

        assertEquals(
                List.of("Zulu", "Alpha"),
                titles(higherViews, higherRating));
    }

    @Test
    void TID063_viewsTakePriorityOverAlphabeticalTitle() {
        var moreViews = recommendation("Zulu", 50, 4.5, 1000);
        var alphabeticallyFirst = recommendation("Alpha", 50, 4.5, 100);

        assertEquals(
                List.of("Zulu", "Alpha"),
                titles(alphabeticallyFirst, moreViews));
    }

    @Test
    void TID063_appliesAllTieBreakersInCorrectOrder() {
        var comet = recommendation("Comet", 11, 4.5, 1500);
        var beacon = recommendation("Beacon", 11, 4.5, 1500);
        var apollo = recommendation("Apollo", 11, 4.5, 2000);
        var aurora = recommendation("Aurora", 11, 4.6, 1000);

        assertEquals(
                List.of("Aurora", "Apollo", "Beacon", "Comet"),
                titles(comet, beacon, apollo, aurora));
    }

    @Test
    void TID063_producesSameOrderRegardlessOfInputOrder() {
        var alpha = recommendation("Alpha", 20, 4.5, 100);
        var beta = recommendation("Beta", 20, 4.5, 100);
        var gamma = recommendation("Gamma", 20, 4.5, 100);

        var expected = List.of("Alpha", "Beta", "Gamma");

        assertEquals(expected, titles(gamma, alpha, beta));
        assertEquals(expected, titles(beta, gamma, alpha));
        assertEquals(expected, titles(alpha, beta, gamma));
    }
}
