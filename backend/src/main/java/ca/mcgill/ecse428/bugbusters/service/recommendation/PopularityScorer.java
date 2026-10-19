package ca.mcgill.ecse428.bugbusters.service.recommendation;

import ca.mcgill.ecse428.bugbusters.model.Movie;

/**
 * TODO TID058: Calculate averageRating * 2 + min(ratingCount, 1000) / 100.0
 * + min(commentCount, 500) / 100.0. Preserve fractional bonuses.
 */
public class PopularityScorer {
    public double score(Movie movie) {
        throw new UnsupportedOperationException("TID058: PopularityScorer is not implemented");
    }
}
