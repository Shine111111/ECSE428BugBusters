package ca.mcgill.ecse428.bugbusters.service.recommendation;
import ca.mcgill.ecse428.bugbusters.model.*;
import java.util.*;
public class RatingHistoryScorer {
    /** Each rated movie contributes once per matching category, even with multiple shared genres. */
    public double score(Movie movie, List<Review> reviews, Map<String, Movie> moviesById) {
        double score = 0;
        for (Review review : reviews) {
            if (review.getRating() < 1 || review.getRating() > 5)
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            Movie rated = moviesById.get(review.getMovieId());
            if (rated == null || review.getRating() == 3) continue;
            boolean positive = review.getRating() >= 4;
            if (!Collections.disjoint(movie.getGenres(), rated.getGenres())) score += positive ? 15 : -20;
            if (!Collections.disjoint(movie.getActors(), rated.getActors())) score += positive ? 10 : -15;
            if (Objects.equals(movie.getDirector(), rated.getDirector())) score += positive ? 10 : -15;
        }
        return score;
    }
}
