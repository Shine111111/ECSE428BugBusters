package ca.mcgill.ecse428.bugbusters.unit;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie inception;
    private Movie interstellar;
    private Movie darkKnight;
    private Movie pulpFiction;

    @BeforeEach
    void setUp() {
        inception = new Movie("tt1375666", "Inception", "Sci-Fi", 2010, 148, new BigDecimal("8.8"), 2400000L);
        interstellar = new Movie("tt0816692", "Interstellar", "Sci-Fi", 2014, 169, new BigDecimal("8.7"), 1900000L);
        darkKnight = new Movie("tt0468569", "The Dark Knight", "Action", 2008, 152, new BigDecimal("9.0"), 2700000L);
        pulpFiction = new Movie("tt0110912", "Pulp Fiction", "Crime", 1994, 154, new BigDecimal("8.9"), 2100000L);
    }

    // =========================================================================
    // 1. Task: Write unit tests for individual search criteria
    // =========================================================================
    @Nested
    @DisplayName("Individual Criteria Search Tests")
    class IndividualCriteriaTests {

        @Test
        @DisplayName("Search by title returns matching movies")
        void testSearchByTitle_Success() {
            when(movieRepository.findByTitleContainingIgnoreCase("Inception"))
                    .thenReturn(List.of(inception));

            List<Movie> results = movieService.searchMoviesByTitle("Inception");

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("Inception", results.get(0).getTitle());
            verify(movieRepository, times(1)).findByTitleContainingIgnoreCase("Inception");
        }

        @Test
        @DisplayName("Search by genre returns matching movies")
        void testSearchByGenre_Success() {
            when(movieRepository.findByGenreContainingIgnoreCase("Sci-Fi"))
                    .thenReturn(List.of(inception, interstellar));

            List<Movie> results = movieService.searchMoviesByGenre("Sci-Fi");

            assertNotNull(results);
            assertEquals(2, results.size());
            verify(movieRepository, times(1)).findByGenreContainingIgnoreCase("Sci-Fi");
        }

        @Test
        @DisplayName("Search by actor returns movies with that actor")
        void testSearchByActor_Success() {
            when(movieRepository.findMoviesByActorNameContainingIgnoreCase("Leonardo DiCaprio"))
                    .thenReturn(List.of(inception));

            List<Movie> results = movieService.searchMoviesByActor("Leonardo DiCaprio");

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("Inception", results.get(0).getTitle());
            verify(movieRepository, times(1)).findMoviesByActorNameContainingIgnoreCase("Leonardo DiCaprio");
        }

        @Test
        @DisplayName("Search by director returns movies directed by that person")
        void testSearchByDirector_Success() {
            when(movieRepository.findMoviesByDirectorNameContainingIgnoreCase("Christopher Nolan"))
                    .thenReturn(List.of(inception, interstellar, darkKnight));

            List<Movie> results = movieService.searchMoviesByDirector("Christopher Nolan");

            assertNotNull(results);
            assertEquals(3, results.size());
            verify(movieRepository, times(1)).findMoviesByDirectorNameContainingIgnoreCase("Christopher Nolan");
        }
    }

    // =========================================================================
    // 2. Task: Write unit tests for multiple matching results
    // =========================================================================
    @Nested
    @DisplayName("Multiple Matching Results Tests")
    class MultipleMatchingResultsTests {

        @Test
        @DisplayName("General search dispatcher with 'director' returns multiple distinct titles")
        void testSearchDispatcher_MultipleDirectorMatches() {
            when(movieRepository.findMoviesByDirectorNameContainingIgnoreCase("Nolan"))
                    .thenReturn(List.of(inception, interstellar, darkKnight));

            List<Movie> results = movieService.searchMovies("director", "Nolan");

            assertEquals(3, results.size());
            assertTrue(results.stream().anyMatch(m -> m.getTitle().equals("Inception")));
            assertTrue(results.stream().anyMatch(m -> m.getTitle().equals("Interstellar")));
            assertTrue(results.stream().anyMatch(m -> m.getTitle().equals("The Dark Knight")));
        }

        @Test
        @DisplayName("General search with fallback searchMovies returns multiple matches across title/genre")
        void testSearchDispatcher_FallbackQueryMatches() {
            when(movieRepository.searchMovies("Sci-Fi"))
                    .thenReturn(List.of(inception, interstellar));

            List<Movie> results = movieService.searchMovies("unknown_criterion", "Sci-Fi");

            assertEquals(2, results.size());
            verify(movieRepository, times(1)).searchMovies("Sci-Fi");
        }

        @Test
        @DisplayName("Query returning zero results returns empty list without error")
        void testSearch_ZeroMatches() {
            when(movieRepository.findByTitleContainingIgnoreCase("NonExistentMovie"))
                    .thenReturn(Collections.emptyList());

            List<Movie> results = movieService.searchMoviesByTitle("NonExistentMovie");

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    // =========================================================================
    // 3. Task: Write tests for error and edge cases for searching
    // =========================================================================
    @Nested
    @DisplayName("Error and Edge Cases Tests")
    class ErrorAndEdgeCaseTests {

        @Test
        @DisplayName("Empty query throws IllegalArgumentException")
        void testSearch_EmptyQueryThrows() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> movieService.searchMovies("title", "")
            );
            assertEquals("Search query cannot be empty", exception.getMessage());
            verify(movieRepository, never()).findByTitleContainingIgnoreCase(anyString());
        }

        @Test
        @DisplayName("Whitespace-only query throws IllegalArgumentException")
        void testSearch_WhitespaceQueryThrows() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> movieService.searchMoviesByActor("    ")
            );
            assertEquals("Search query cannot be empty", exception.getMessage());
            verify(movieRepository, never()).findMoviesByActorNameContainingIgnoreCase(anyString());
        }

        @Test
        @DisplayName("Null query throws IllegalArgumentException")
        void testSearch_NullQueryThrows() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> movieService.searchMoviesByDirector(null)
            );
            assertEquals("Search query cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Find movie by non-existent ID throws IllegalArgumentException")
        void testFindMovieById_NotFoundThrows() {
            when(movieRepository.findById("invalid_id")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> movieService.findMovieById("invalid_id")
            );
            assertTrue(exception.getMessage().contains("Movie not found: invalid_id"));
        }

        @Test
        @DisplayName("Find movie by blank ID throws IllegalArgumentException")
        void testFindMovieById_BlankIdThrows() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> movieService.findMovieById("  ")
            );
            assertEquals("Movie ID cannot be empty", exception.getMessage());
        }
    }
}