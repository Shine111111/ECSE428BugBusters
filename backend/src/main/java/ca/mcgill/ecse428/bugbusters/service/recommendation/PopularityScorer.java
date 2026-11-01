package ca.mcgill.ecse428.bugbusters.service.recommendation;
import ca.mcgill.ecse428.bugbusters.model.Movie;
public class PopularityScorer {
    public double score(Movie movie) {
        return movie.getAverageRating() * 2 + Math.min(movie.getRatingCount(), 1000) / 100.0
            + Math.min(movie.getCommentCount(), 500) / 100.0;
    }
}
