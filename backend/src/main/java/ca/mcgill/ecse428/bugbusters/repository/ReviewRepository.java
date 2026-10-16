package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.Review;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepository {
    private final Map<String, Review> reviews = new HashMap<>();

    private String key(String userId, String movieId) {
        return userId + ":" + movieId;
    }

    public void save(Review review) {
        reviews.put(key(review.getUserId(), review.getMovieId()), review);
    }

    public Review findByUserAndMovie(String userId, String movieId) {
        return reviews.get(key(userId, movieId));
    }

    public List<Review> findByMovie(String movieId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getMovieId().equals(movieId)) {
                result.add(review);
            }
        }
        return result;
    }

    public List<Review> findByUser(String userId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getUserId().equals(userId)) {
                result.add(review);
            }
        }
        return result;
    }

    public List<Review> findByMovieAndRating(String movieId, int rating) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getMovieId().equals(movieId)
                    && review.getRating() == rating) {
                result.add(review);
            }
        }
        return result;
    }
}