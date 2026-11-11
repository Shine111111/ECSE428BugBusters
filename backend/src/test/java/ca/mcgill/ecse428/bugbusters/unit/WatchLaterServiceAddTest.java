package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.dto.WatchLaterResponse;
import ca.mcgill.ecse428.bugbusters.model.WatchLaterEntry;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.repository.WatchLaterRepository;
import ca.mcgill.ecse428.bugbusters.service.WatchLaterService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TID027: unit tests for adding a movie to Watch Later, normal and first movie cases
// (repositories are mocked, the clock is fixed)
@ExtendWith(MockitoExtension.class)
class WatchLaterServiceAddTest {

    private static final String USER_ID = "0001";
    private static final String MOVIE_ID = "tt0110912";
    private static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");

    @Mock
    private WatchLaterRepository watchLaterRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserRepository userRepository;

    private WatchLaterService service;

    @BeforeEach
    void setUp() {
        service = new WatchLaterService(watchLaterRepository, movieRepository, userRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    // valid user and movie, not yet in the playlist, which already holds `existingCount` movies
    private void stubValidAdd(String movieId, long existingCount) {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(movieRepository.existsById(movieId)).thenReturn(true);
        when(watchLaterRepository.existsByUserIdAndMovieId(USER_ID, movieId)).thenReturn(false);
        when(watchLaterRepository.countByUserId(USER_ID)).thenReturn(existingCount);
    }

    private WatchLaterEntry savedEntry() {
        ArgumentCaptor<WatchLaterEntry> captor = ArgumentCaptor.forClass(WatchLaterEntry.class);
        verify(watchLaterRepository).save(captor.capture());
        return captor.getValue();
    }

    // ---- normal flow: the playlist already has movies ----

    @Test
    void addingToNonEmptyPlaylistReturnsAddedMessage() {
        stubValidAdd(MOVIE_ID, 1);

        WatchLaterResponse response = service.addMovieToWatchLater(USER_ID, MOVIE_ID);

        assertEquals(new WatchLaterResponse(USER_ID, MOVIE_ID, WatchLaterService.MOVIE_ADDED), response);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 10, 500})
    void anyNonEmptyPlaylistGetsTheNormalMessage(long existingCount) {
        stubValidAdd(MOVIE_ID, existingCount);

        assertEquals(WatchLaterService.MOVIE_ADDED, service.addMovieToWatchLater(USER_ID, MOVIE_ID).message());
    }

    @Test
    void addingSavesTheUserMoviePairWithTheCurrentTime() {
        stubValidAdd(MOVIE_ID, 3);

        service.addMovieToWatchLater(USER_ID, MOVIE_ID);

        WatchLaterEntry saved = savedEntry();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(MOVIE_ID, saved.getMovieId());
        assertEquals(NOW, saved.getAddedAt());
    }

    @Test
    void addedMessageText() {
        assertEquals("This movie was added to your Watch Later playlist.", WatchLaterService.MOVIE_ADDED);
    }

    // ---- alternate flow: first movie ----

    @Test
    void addingToEmptyPlaylistReturnsFirstMovieMessage() {
        stubValidAdd(MOVIE_ID, 0);

        WatchLaterResponse response = service.addMovieToWatchLater(USER_ID, MOVIE_ID);

        assertEquals(new WatchLaterResponse(USER_ID, MOVIE_ID, WatchLaterService.FIRST_MOVIE_ADDED), response);
    }

    @Test
    void firstMovieIsSaved() {
        stubValidAdd(MOVIE_ID, 0);

        service.addMovieToWatchLater(USER_ID, MOVIE_ID);

        WatchLaterEntry saved = savedEntry();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(MOVIE_ID, saved.getMovieId());
        assertEquals(NOW, saved.getAddedAt());
    }

    @Test
    void firstMovieMessageText() {
        assertEquals("You have added your first movie to your Watch Later playlist.",
                WatchLaterService.FIRST_MOVIE_ADDED);
    }

    @Test
    void secondMovieGetsTheNormalMessage() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(movieRepository.existsById("tt0110912")).thenReturn(true);
        when(movieRepository.existsById("tt0816692")).thenReturn(true);
        when(watchLaterRepository.countByUserId(USER_ID)).thenReturn(0L, 1L);

        assertEquals(WatchLaterService.FIRST_MOVIE_ADDED,
                service.addMovieToWatchLater(USER_ID, "tt0110912").message());
        assertEquals(WatchLaterService.MOVIE_ADDED,
                service.addMovieToWatchLater(USER_ID, "tt0816692").message());
    }

    // ---- listing the playlist ----

    @Test
    void listReturnsMovieIdsInRepositoryOrder() {
        when(watchLaterRepository.findByUserIdOrderByAddedAtDescIdDesc(USER_ID)).thenReturn(List.of(
                new WatchLaterEntry(USER_ID, "tt0468569", NOW.plusSeconds(20)),
                new WatchLaterEntry(USER_ID, "tt0816692", NOW.plusSeconds(10)),
                new WatchLaterEntry(USER_ID, "tt0110912", NOW)));

        assertEquals(List.of("tt0468569", "tt0816692", "tt0110912"), service.getWatchLaterMovieIds(USER_ID));
    }

    @Test
    void listOfEmptyPlaylistIsEmpty() {
        when(watchLaterRepository.findByUserIdOrderByAddedAtDescIdDesc(USER_ID)).thenReturn(List.of());

        assertTrue(service.getWatchLaterMovieIds(USER_ID).isEmpty());
    }
}
