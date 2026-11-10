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

class ReviewServiceDeleteTest {
    private ReviewRepository repository;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository();
        service = new ReviewService(repository);
    }

    @Test
    void ID_008_testDeleteReview() {
        service.storeReview(new Review("TestUser", "Cars2", 4, "Great movie"));
        service.deleteReview("TestUser", "Cars2");
        assertNull(repository.findByUserAndMovie("TestUser", "Cars2"));
    }

    @Test
    void ID_008_testDeleteNonExistentReview() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.deleteReview("User", "Movie"));
        assertEquals("Review does not exist", error.getMessage());
    }

    @Test
    void ID_008_deleteDoesNotAffectOtherReviews() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        service.deleteReview("Hamza", "Cars2");
        assertNotNull(repository.findByUserAndMovie("Robert", "Cars2"));
        assertNotNull(repository.findByUserAndMovie("Hamza", "Up"));
    }

    @Test
    void ID_008_deletedReviewCanBeCreatedAgain() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.deleteReview("Hamza", "Cars2");
        service.storeReview(new Review("Hamza", "Cars2", 1, null));
        assertEquals(1, repository.findByUserAndMovie("Hamza", "Cars2").getRating());
    }
}
