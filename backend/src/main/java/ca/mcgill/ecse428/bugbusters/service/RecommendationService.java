package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import ca.mcgill.ecse428.bugbusters.service.recommendation.*;
import java.util.List;

/**
 * Coordinates data retrieval, watched-movie filtering, scoring, and ranking.
 * TODO TID059: Load all data, filter candidates before scoring, sum the three
 * scores, and return the ranked recommendations.
 */
public class RecommendationService {
    private final RecommendationDataProvider dataProvider;
    private final WatchedMovieFilter watchedMovieFilter;
    private final PreferenceScorer preferenceScorer;
    private final RatingHistoryScorer ratingHistoryScorer;
    private final PopularityScorer popularityScorer;
    private final RecommendationRanker recommendationRanker;

    public RecommendationService(
            RecommendationDataProvider dataProvider,
            WatchedMovieFilter watchedMovieFilter,
            PreferenceScorer preferenceScorer,
            RatingHistoryScorer ratingHistoryScorer,
            PopularityScorer popularityScorer,
            RecommendationRanker recommendationRanker) {
        this.dataProvider = dataProvider;
        this.watchedMovieFilter = watchedMovieFilter;
        this.preferenceScorer = preferenceScorer;
        this.ratingHistoryScorer = ratingHistoryScorer;
        this.popularityScorer = popularityScorer;
        this.recommendationRanker = recommendationRanker;
    }

    public List<MovieRecommendationResponse> getRecommendations(String userId) {
        throw new UnsupportedOperationException("TID059: Recommendation orchestration is not implemented");
    }
}
