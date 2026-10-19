package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import java.util.List;

/**
 * Sort by total score descending, average rating descending, view count descending,
 * then title ascending, as required by SID0021. TODO: Implement ranking; TID060 covers
 * scoring, filtering, ranking, and tie-breaking tests.
 */
public class RecommendationRanker {
    public List<MovieRecommendationResponse> rank(List<MovieRecommendationResponse> recommendations) {
        throw new UnsupportedOperationException("SID0021: RecommendationRanker is not implemented");
    }
}
