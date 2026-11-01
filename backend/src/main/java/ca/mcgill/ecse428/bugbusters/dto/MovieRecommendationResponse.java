package ca.mcgill.ecse428.bugbusters.dto;

import ca.mcgill.ecse428.bugbusters.model.Movie;

/** Holds one recommended movie and its combined preference, rating-history, and popularity score. */
public class MovieRecommendationResponse {
    private final Movie movie;
    private final double totalScore;

    public MovieRecommendationResponse(Movie movie, double totalScore) {
        this.movie = movie;
        this.totalScore = totalScore;
    }

    public Movie getMovie() { return movie; }
    public double getTotalScore() { return totalScore; }
}
