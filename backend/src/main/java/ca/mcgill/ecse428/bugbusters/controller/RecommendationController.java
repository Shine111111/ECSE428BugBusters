package ca.mcgill.ecse428.bugbusters.controller;

import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import ca.mcgill.ecse428.bugbusters.service.RecommendationService;
import java.util.List;

/**
 * Entry point for personalized recommendations.
 * TODO TID054: Map GET /recommendations using the chosen HTTP framework and
 * obtain the user ID from the authenticated session.
 */
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public List<MovieRecommendationResponse> getRecommendations(String userId) {
        return recommendationService.getRecommendations(userId);
    }
}
