package ca.mcgill.ecse428.bugbusters.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;

public class ReviewServiceDeleteTest {
    private ReviewRepository repository;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository();
        service = new ReviewService(repository);
    }

    @Test 
    public void testDeleteReview() {
        service.storeReview(new Review("TestUser", "Cars2", 4, "Great movie"));
        service.deleteReview("TestUser", "Cars2");
        assert(repository.findByUserAndMovie("TestUser", "Cars2") == null);
    }

    @Test
    public void testDeleteNonExistentReview() {
        service.deleteReview("User", "Movie");
        assert(repository.findByUserAndMovie("User", "Movie") == null);
    }
    
}
