package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Displays a user's rating/review history
class ReviewServiceHistoryTest {
    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(new ReviewRepository());
    }

    @Test
    void returnsAllReviewsForUser() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        service.storeReview(new Review("Hamza", "Cars3", 2, "Too slow"));
        List<Review> history = service.getReviewsForUser("Hamza");

        assertEquals(3, history.size());
        assertEquals(Set.of("Cars2", "Up", "Cars3"),
                history.stream().map(Review::getMovieId).collect(Collectors.toSet()));
    }

    @Test
    void returnsOnlyReviewsBelongingToUser() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.storeReview(new Review("Robert", "Up", 3, null));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        List<Review> history = service.getReviewsForUser("Hamza");
        assertEquals(2, history.size());
        assertTrue(history.stream().allMatch(r -> r.getUserId().equals("Hamza")));
    }

    @Test
    void returnsEmptyListForUserWithNoReviews() {
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        List<Review> history = service.getReviewsForUser("Hamza");
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void returnsEmptyListWhenNoReviewsExist() {
        assertTrue(service.getReviewsForUser("Hamza").isEmpty());
    }

    @Test
    void userIdMatchIsExact() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Hamza2", "Up", 5, null));
        assertEquals(1, service.getReviewsForUser("Hamza").size());
        assertTrue(service.getReviewsForUser("hamza").isEmpty());
    }

    @Test
    void historyReflectsUpdatedReview() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Great movie"));
        service.updateReview("Hamza", "Cars2", 2, "Worse on rewatch");
        List<Review> history = service.getReviewsForUser("Hamza");
        assertEquals(1, history.size());
        assertEquals(2, history.get(0).getRating());
        assertEquals("Worse on rewatch", history.get(0).getReviewText());
    }
}