package ca.mcgill.ecse428.bugbusters.model;

public class Review {
    private final String userId;
    private final String movieId;
    private int rating;
    private String reviewText;

    public Review(String userId, String movieId, int rating, String reviewText) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public String getUserId() {
        return userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}