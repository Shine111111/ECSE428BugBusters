package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceSubmitTest {
    @Mock
    private MovieRepository movieRepository;

    private ReviewRepository repository;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository();
        service = new ReviewService(repository, movieRepository);
    }

    @Test
    void loggedInUserCanRateExistingMovie() {
        when(movieRepository.existsById("Cars2")).thenReturn(true);
        service.storeReview(new Review("Hamza", "Cars2", 4, null), "Hamza");
        assertNotNull(repository.findByUserAndMovie("Hamza", "Cars2"));
    }

    @Test
    void rejectRatingWhenNotLoggedIn() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", "Cars2", 4, null), null));
        assertEquals("User must be logged in to rate a movie", error.getMessage());
        assertNull(repository.findByUserAndMovie("Hamza", "Cars2"));
    }

    @Test
    void rejectRatingForAnotherUser() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Robert", "Cars2", 4, null), "Hamza"));
        assertEquals("Users can only rate movies as themselves", error.getMessage());
        assertNull(repository.findByUserAndMovie("Robert", "Cars2"));
    }

    @Test
    void rejectRatingForNonexistentMovie() {
        when(movieRepository.existsById("UNKNOWN_MOVIE")).thenReturn(false);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.storeReview(new Review("Hamza", "UNKNOWN_MOVIE", 4, null), "Hamza"));
        assertEquals("Movie does not exist", error.getMessage());
        assertNull(repository.findByUserAndMovie("Hamza", "UNKNOWN_MOVIE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "3", "5", " 4 "})
    void parseRatingAcceptsWholeNumbersFromOneToFive(String raw) {
        assertEquals(Integer.parseInt(raw.trim()), service.parseRating(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "6", "-1", "2.5", "abc", ""})
    void parseRatingRejectsInvalidValues(String raw) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.parseRating(raw));
        assertEquals("Rating must be between 1 and 5", error.getMessage());
    }

    @Test
    void parseRatingRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.parseRating(null));
    }
}
