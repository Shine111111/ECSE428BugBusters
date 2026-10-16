package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

//Filter by rating
class ReviewServiceFilterTest {
    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(new ReviewRepository());
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        service.storeReview(new Review("Robert", "Cars2", 4, null));
        service.storeReview(new Review("Alice", "Cars2", 2, "Too slow"));
        service.storeReview(new Review("Hamza", "Up", 4, null));
    }

    @Test
    void returnsReviewsMatchingMovieAndRating() {
        List<Review> result = service.getReviewsForMovieByRating("Cars2", 4);
        assertEquals(2, result.size());
        assertEquals(Set.of("Hamza", "Robert"),
                result.stream().map(Review::getUserId).collect(Collectors.toSet()));
        assertTrue(result.stream().allMatch(
                r -> r.getMovieId().equals("Cars2") && r.getRating() == 4));
    }

    @Test
    void excludesSameRatingOnOtherMovies() {
        List<Review> result = service.getReviewsForMovieByRating("Up", 4);
        assertEquals(1, result.size());
        assertEquals("Hamza", result.get(0).getUserId());
    }

    @Test
    void returnsEmptyListWhenNoReviewHasRating() {
        List<Review> result = service.getReviewsForMovieByRating("Cars2", 5);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyListForMovieWithNoReviews() {
        assertTrue(service.getReviewsForMovieByRating("Cars3", 4).isEmpty());
    }

    @Test
    void filterReflectsUpdatedRating() {
        service.updateReview("Alice", "Cars2", 4, "Grew on me");
        assertEquals(3, service.getReviewsForMovieByRating("Cars2", 4).size());
        assertTrue(service.getReviewsForMovieByRating("Cars2", 2).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void acceptsEveryValidRating(int stars) {
        assertNotNull(service.getReviewsForMovieByRating("Cars2", stars));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 6, 100})
    void rejectsRatingOutsideOneToFive(int stars) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.getReviewsForMovieByRating("Cars2", stars));
        assertEquals("Rating must be between 1 and 5", e.getMessage());
    }
}