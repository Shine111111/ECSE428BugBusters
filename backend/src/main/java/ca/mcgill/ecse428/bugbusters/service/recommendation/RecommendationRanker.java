package ca.mcgill.ecse428.bugbusters.service.recommendation;
import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import java.util.*;
public class RecommendationRanker {
    public List<MovieRecommendationResponse> rank(List<MovieRecommendationResponse> recommendations) {
        return recommendations.stream().sorted(Comparator
            .comparingDouble(MovieRecommendationResponse::getTotalScore).reversed()
            .thenComparing(Comparator.comparingDouble((MovieRecommendationResponse r) -> r.getMovie().getAverageRating()).reversed())
            .thenComparing(Comparator.comparingInt((MovieRecommendationResponse r) -> r.getMovie().getViewCount()).reversed())
            .thenComparing(r -> r.getMovie().getTitle())).toList();
    }
}
