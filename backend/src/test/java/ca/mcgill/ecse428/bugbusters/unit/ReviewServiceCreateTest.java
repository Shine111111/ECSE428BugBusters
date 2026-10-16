package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

//Unit tests for creating a rating
class ReviewServiceCreateTest {
    private ReviewRepository repository;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository();
        service = new ReviewService(repository);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void createValidRatingWithoutReviewText(int stars) {
        service.storeReview(new Review("Hamza", "Cars2", stars, null));
        Review stored = repository.findByUserAndMovie("Hamza", "Cars2");
        assertNotNull(stored);
        assertEquals(stars, stored.getRating());
        assertNull(stored.getReviewText());
    }

    @Test
    void createValidRatingWithReviewText() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        Review stored = repository.findByUserAndMovie("Hamza", "Cars2");
        assertNotNull(stored);
        assertEquals(4, stored.getRating());
        assertEquals("Great movie", stored.getReviewText());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void rejectRatingBelowOne(int stars) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", "Cars2", stars, null)));
        assertEquals("Rating must be between 1 and 5", e.getMessage());
        assertNull(repository.findByUserAndMovie("Hamza", "Cars2"));
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 10, Integer.MAX_VALUE})
    void rejectRatingAboveFive(int stars) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", "Cars2", stars, null)));
        assertEquals("Rating must be between 1 and 5", e.getMessage());
        assertNull(repository.findByUserAndMovie("Hamza", "Cars2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectBlankUserId(String userId) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review(userId, "Cars2", 4, null)));
        assertEquals("User ID cannot be blank", e.getMessage());
        assertEquals(0, repository.findByMovie("Cars2").size());
    }

    @Test
    void rejectNullUserId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review(null, "Cars2", 4, null)));
        assertEquals("User ID cannot be blank", e.getMessage());
        assertEquals(0, repository.findByMovie("Cars2").size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectBlankMovieId(String movieId) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", movieId, 4, null)));
        assertEquals("Movie ID cannot be blank", e.getMessage());
        assertEquals(0, repository.findByUser("Hamza").size());
    }

    @Test
    void rejectNullMovieId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", null, 4, null)));
        assertEquals("Movie ID cannot be blank", e.getMessage());
        assertEquals(0, repository.findByUser("Hamza").size());
    }

    @Test
    void rejectNullReview() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(null));
        assertEquals("Review cannot be null", e.getMessage());
    }

    @Test
    void rejectDuplicateRatingForSameUserAndMovie() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", "Cars2", 2, "Too slow")));

        assertEquals("User has already rated this movie", e.getMessage());
        Review stored = repository.findByUserAndMovie("Hamza", "Cars2");
        assertEquals(4, stored.getRating());
        assertEquals("Great movie", stored.getReviewText());
        assertEquals(1, repository.findByMovie("Cars2").size());
    }

    @Test
    void allowDifferentUsersToRateSameMovie() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        assertEquals(4, repository.findByUserAndMovie("Hamza", "Cars2").getRating());
        assertEquals(2, repository.findByUserAndMovie("Robert", "Cars2").getRating());
        assertEquals(2, repository.findByMovie("Cars2").size());
    }
}