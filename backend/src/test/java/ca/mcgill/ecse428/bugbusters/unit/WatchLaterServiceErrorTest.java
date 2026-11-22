package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.model.WatchLaterEntry;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.repository.WatchLaterRepository;
import ca.mcgill.ecse428.bugbusters.service.WatchLaterService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TID029: unit tests for duplicate and invalid input handling in the Watch Later service
// and the error messages it returns (repositories are mocked)
@ExtendWith(MockitoExtension.class)
class WatchLaterServiceErrorTest {

    private static final String USER_ID = "0001";
    private static final String MOVIE_ID = "tt0110912";

    @Mock
    private WatchLaterRepository watchLaterRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserRepository userRepository;

    private WatchLaterService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC);
        service = new WatchLaterService(watchLaterRepository, movieRepository, userRepository, clock);
    }

    private void assertRejected(String expectedMessage, String userId, String movieId) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.addMovieToWatchLater(userId, movieId));
        assertEquals(expectedMessage, e.getMessage());
        verify(watchLaterRepository, never()).save(any(WatchLaterEntry.class));
    }

    // ---- duplicate movie ----

    @Test
    void rejectsMovieAlreadyInPlaylist() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(watchLaterRepository.existsByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(true);

        assertRejected(WatchLaterService.MOVIE_ALREADY_ADDED, USER_ID, MOVIE_ID);
        verify(watchLaterRepository, never()).countByUserId(any());
    }

    @Test
    void duplicateErrorMessageMatchesAcceptanceTest() {
        assertEquals("This movie has already been added to the playlist.", WatchLaterService.MOVIE_ALREADY_ADDED);
    }

    // ---- unknown movie ----

    @Test
    void rejectsMovieNotInCatalog() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(movieRepository.existsById("tt9999999")).thenReturn(false);

        assertRejected(WatchLaterService.MOVIE_NOT_EXIST, USER_ID, "tt9999999");
        verifyNoInteractions(watchLaterRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void rejectsMissingMovieId(String movieId) {
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        assertRejected(WatchLaterService.MOVIE_NOT_EXIST, USER_ID, movieId);
        verifyNoInteractions(movieRepository, watchLaterRepository);
    }

    @Test
    void unknownMovieErrorMessage() {
        assertEquals("This movie was not found.", WatchLaterService.MOVIE_NOT_EXIST);
    }

    // ---- unknown user ----

    @Test
    void rejectsUnknownUser() {
        when(userRepository.existsById("9999")).thenReturn(false);

        assertRejected(WatchLaterService.USER_NOT_FOUND, "9999", MOVIE_ID);
        verifyNoInteractions(movieRepository, watchLaterRepository);
    }

    @Test
    void unknownUserIsReportedBeforeUnknownMovie() {
        when(userRepository.existsById("9999")).thenReturn(false);

        assertRejected(WatchLaterService.USER_NOT_FOUND, "9999", "tt9999999");
        verifyNoInteractions(movieRepository);
    }

    @Test
    void unknownUserErrorMessage() {
        assertEquals("User not found", WatchLaterService.USER_NOT_FOUND);
    }

    // ---- not logged in (missing user ID) ----

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void rejectsMissingUserId(String userId) {
        assertRejected(WatchLaterService.LOGIN_REQUIRED, userId, MOVIE_ID);
        verifyNoInteractions(userRepository, movieRepository, watchLaterRepository);
    }

    @Test
    void missingUserIsReportedBeforeMissingMovie() {
        assertRejected(WatchLaterService.LOGIN_REQUIRED, null, null);
        verifyNoInteractions(userRepository, movieRepository, watchLaterRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void listingPlaylistRequiresLogin(String userId) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.getWatchLaterMovieIds(userId));
        assertEquals(WatchLaterService.LOGIN_REQUIRED, e.getMessage());
        verifyNoInteractions(watchLaterRepository);
    }

    @Test
    void loginRequiredErrorMessage() {
        assertEquals("You must be logged in to use Watch Later", WatchLaterService.LOGIN_REQUIRED);
    }

    // ---- a rejected add leaves the playlist alone; a different movie still goes through ----

    @Test
    void duplicateDoesNotBlockAddingADifferentMovie() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(movieRepository.existsById(any())).thenReturn(true);
        when(watchLaterRepository.existsByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(true);
        when(watchLaterRepository.existsByUserIdAndMovieId(USER_ID, "tt0816692")).thenReturn(false);
        when(watchLaterRepository.countByUserId(USER_ID)).thenReturn(1L);

        assertRejected(WatchLaterService.MOVIE_ALREADY_ADDED, USER_ID, MOVIE_ID);
        assertEquals(WatchLaterService.MOVIE_ADDED,
                service.addMovieToWatchLater(USER_ID, "tt0816692").message());
        verify(watchLaterRepository).save(any(WatchLaterEntry.class));
    }
}
