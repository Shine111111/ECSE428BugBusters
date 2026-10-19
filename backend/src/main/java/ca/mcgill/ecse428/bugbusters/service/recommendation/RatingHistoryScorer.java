package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Review;
import java.util.List;
import java.util.Map;

/**
 * TODO TID057: Compare candidates with rated movie metadata. Ratings of 4 or 5
 * add 15 for shared genres and 10 for shared actors or directors; ratings of 1 or 2
 * subtract 20 and 15 respectively. Three-star ratings are neutral. Follow the feature
 * scenarios when deciding how overlapping matches accumulate.
 */
public class RatingHistoryScorer {
    public double score(Movie movie, List<Review> reviews, Map<String, Movie> moviesById) {
        throw new UnsupportedOperationException("TID057: RatingHistoryScorer is not implemented");
    }
}
