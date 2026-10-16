package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

class ReviewServiceRetrieveUpdateTest {
    private ReviewRepository repository;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository();
        service = new ReviewService(repository);
    }

    @Test
    void getReviewsForMovieReturnsOnlyThatMovie() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        List<Review> result = service.getReviewsForMovie("Cars2");
        assertEquals(2, result.size());
        assertEquals(Set.of("Hamza", "Robert"),
                result.stream().map(Review::getUserId).collect(Collectors.toSet()));
    }

    @Test
    void getReviewsForMovieReturnsEmptyListWhenNoneExist() {
        assertTrue(service.getReviewsForMovie("Cars2").isEmpty());
    }

    @Test
    void updateChangesRatingAndText() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        service.updateReview("Hamza", "Cars2", 2, "Worse on rewatch");
        Review stored = repository.findByUserAndMovie("Hamza", "Cars2");
        assertEquals(2, stored.getRating());
        assertEquals("Worse on rewatch", stored.getReviewText());
        assertEquals(1, service.getReviewsForMovie("Cars2").size());
    }

    @Test
    void updateCanClearReviewText() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        service.updateReview("Hamza", "Cars2", 4, null);
        assertNull(repository.findByUserAndMovie("Hamza", "Cars2").getReviewText());
    }

    @Test
    void updateNonexistentReviewIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.updateReview("Hamza", "Cars2", 3, null));
        assertEquals("Review does not exist", e.getMessage());
        assertNull(repository.findByUserAndMovie("Hamza", "Cars2"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    void updateWithInvalidRatingLeavesReviewUnchanged(int stars) {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.updateReview("Hamza", "Cars2", stars, "Changed"));
        assertEquals("Rating must be between 1 and 5", e.getMessage());
        Review stored = repository.findByUserAndMovie("Hamza", "Cars2");
        assertEquals(4, stored.getRating());
        assertEquals("Great movie", stored.getReviewText());
    }

    @Test
    void updateDoesNotAffectOtherUsersReviews() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.updateReview("Hamza", "Cars2", 1, null);
        assertEquals(2, repository.findByUserAndMovie("Robert", "Cars2").getRating());
    }
}