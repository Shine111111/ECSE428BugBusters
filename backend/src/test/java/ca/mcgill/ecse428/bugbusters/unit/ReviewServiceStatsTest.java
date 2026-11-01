package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewServiceStatsTest {
    private ReviewRepository repository;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository();
        service = new ReviewService(repository);
    }

    @Test
    void averageIsNullWhenMovieHasNoReviews() {
        assertNull(service.getAverageRatingForMovie("Cars2"));
    }

    @Test
    void averageOfSingleReviewIsThatRating() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        assertEquals(4.0, service.getAverageRatingForMovie("Cars2"));
    }

    @Test
    void averageCanBeFractional() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 5, null));
        assertEquals(4.5, service.getAverageRatingForMovie("Cars2"));
    }

    @Test
    void averageIgnoresOtherMovies() {
        service.storeReview(new Review("Hamza", "Cars2", 2, null));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        assertEquals(2.0, service.getAverageRatingForMovie("Cars2"));
    }

    @Test
    void averageReflectsUpdatedAndDeletedReviews() {
        service.storeReview(new Review("Hamza", "Cars2", 1, null));
        service.storeReview(new Review("Robert", "Cars2", 3, null));
        service.updateReview("Hamza", "Cars2", 5, null);
        assertEquals(4.0, service.getAverageRatingForMovie("Cars2"));
        service.deleteReview("Robert", "Cars2");
        assertEquals(5.0, service.getAverageRatingForMovie("Cars2"));
    }

    @Test
    void countIsZeroWhenMovieHasNoReviews() {
        assertEquals(0, service.getTotalRatingCountForMovie("Cars2"));
    }

    @Test
    void countIncludesOnlyThatMovie() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        assertEquals(2, service.getTotalRatingCountForMovie("Cars2"));
    }

    @Test
    void countDecreasesAfterDelete() {
        service.storeReview(new Review("Hamza", "Cars2", 4, null));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.deleteReview("Hamza", "Cars2");
        assertEquals(1, service.getTotalRatingCountForMovie("Cars2"));
    }

    @Test
    void getAllRatingsReturnsEveryReviewForMovie() {
        service.storeReview(new Review("Hamza", "Cars2", 4, "Fun"));
        service.storeReview(new Review("Robert", "Cars2", 2, null));
        service.storeReview(new Review("Hamza", "Up", 5, null));
        List<Review> result = service.getAllRatingsForMovie("Cars2");
        assertEquals(2, result.size());
        assertEquals(Set.of("Hamza", "Robert"),
                result.stream().map(Review::getUserId).collect(Collectors.toSet()));
    }

    @Test
    void getAllRatingsIsEmptyWhenMovieHasNoReviews() {
        assertTrue(service.getAllRatingsForMovie("Cars2").isEmpty());
    }
}
