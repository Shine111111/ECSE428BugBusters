package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import java.util.List;

public class ReviewService {
    private final ReviewRepository repository;
    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    //Stores a rating/review
    public void storeReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        validateReview(review);
        if (repository.findByUserAndMovie(
                review.getUserId(), review.getMovieId()) != null) {
            throw new IllegalArgumentException("User has already rated this movie");
        }
        repository.save(review);
    }

    //Retrieves ratings/reviews for a movie
    public List<Review> getReviewsForMovie(String movieId) {
        return repository.findByMovie(movieId);
    }

    //Updates an existing rating/review
    public void updateReview(String userId, String movieId, int rating, String reviewText) {
        Review existing = repository.findByUserAndMovie(userId, movieId);
        if (existing == null) {
            throw new IllegalArgumentException("Review does not exist");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        existing.setRating(rating);
        existing.setReviewText(reviewText);
        repository.save(existing);
    }

    //Retrieves a user's rating/review history
    public List<Review> getReviewsForUser(String userId) {
        return repository.findByUser(userId);
    }

    //Retrieves ratings/reviews for a movie with the given star rating
    public List<Review> getReviewsForMovieByRating(String movieId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return repository.findByMovieAndRating(movieId, rating);
    }

    private void validateReview(Review review) {
        if (review.getUserId() == null || review.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID cannot be blank");
        }
        if (review.getMovieId() == null || review.getMovieId().isBlank()) {
            throw new IllegalArgumentException("Movie ID cannot be blank");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}