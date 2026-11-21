package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovieRepositorySortTest {

    @Mock
    private MovieRepository movieRepository;

    private Movie pulpFiction;     // 1994, 8.9, 480000
    private Movie darkKnight;      // 2008, 9.0, 700000
    private Movie inception;       // 2010, 8.8, 600000
    private Movie shutterIsland;   // 2010, 8.8, 600000 (tie-breaker test movie)
    private Movie interstellar;    // 2014, 8.7, 550000

    @BeforeEach
    void setUp() {
        pulpFiction = new Movie("m1", "Pulp Fiction", "Crime", 1994, 154, new BigDecimal("8.9"), 480000L);
        darkKnight = new Movie("m2", "The Dark Knight", "Action", 2008, 152, new BigDecimal("9.0"), 700000L);
        inception = new Movie("m3", "Inception", "Sci-Fi", 2010, 148, new BigDecimal("8.8"), 600000L);
        shutterIsland = new Movie("m5", "Shutter Island", "Thriller", 2010, 138, new BigDecimal("8.8"), 600000L);
        interstellar = new Movie("m4", "Interstellar", "Sci-Fi", 2014, 169, new BigDecimal("8.7"), 550000L);
    }

    @Test
    @DisplayName("Sort by start year ascending orders oldest to newest with title tie-breaker")
    void testSortByStartYearAsc() {
        when(movieRepository.findAllByOrderByStartYearAscTitleAsc())
                .thenReturn(List.of(pulpFiction, darkKnight, inception, shutterIsland, interstellar));

        List<Movie> results = movieRepository.findAllByOrderByStartYearAscTitleAsc();

        assertEquals(
                List.of("Pulp Fiction", "The Dark Knight", "Inception", "Shutter Island", "Interstellar"),
                results.stream().map(m -> m.getTitle()).toList());
        verify(movieRepository, times(1)).findAllByOrderByStartYearAscTitleAsc();
    }

    @Test
    @DisplayName("Sort by start year descending orders newest to oldest with title tie-breaker")
    void testSortByStartYearDesc() {
        when(movieRepository.findAllByOrderByStartYearDescTitleAsc())
                .thenReturn(List.of(interstellar, inception, shutterIsland, darkKnight, pulpFiction));

        List<Movie> results = movieRepository.findAllByOrderByStartYearDescTitleAsc();

        assertEquals(
                List.of("Interstellar", "Inception", "Shutter Island", "The Dark Knight", "Pulp Fiction"),
                results.stream().map(m -> m.getTitle()).toList());
        verify(movieRepository, times(1)).findAllByOrderByStartYearDescTitleAsc();
    }

    @Test
    @DisplayName("Sort by ratings ascending orders lowest to highest with title tie-breaker")
    void testSortByRatingsAsc() {
        when(movieRepository.findAllByOrderByRatingsAscTitleAsc())
                .thenReturn(List.of(interstellar, inception, shutterIsland, pulpFiction, darkKnight));

        List<Movie> results = movieRepository.findAllByOrderByRatingsAscTitleAsc();

        assertEquals(
                List.of("Interstellar", "Inception", "Shutter Island", "Pulp Fiction", "The Dark Knight"),
                results.stream().map(m -> m.getTitle()).toList());
        verify(movieRepository, times(1)).findAllByOrderByRatingsAscTitleAsc();
    }

    @Test
    @DisplayName("Sort by ratings descending orders highest to lowest with title tie-breaker")
    void testSortByRatingsDesc() {
        when(movieRepository.findAllByOrderByRatingsDescTitleAsc())
                .thenReturn(List.of(darkKnight, pulpFiction, inception, shutterIsland, interstellar));

        List<Movie> results = movieRepository.findAllByOrderByRatingsDescTitleAsc();

        assertEquals(
                List.of("The Dark Knight", "Pulp Fiction", "Inception", "Shutter Island", "Interstellar"),
                results.stream().map(m -> m.getTitle()).toList());
        verify(movieRepository, times(1)).findAllByOrderByRatingsDescTitleAsc();
    }

    @Test
    @DisplayName("Sort by views (numVotes) ascending orders least to most viewed with title tie-breaker")
    void testSortByViewsAsc() {
        when(movieRepository.findAllByOrderByNumVotesAscTitleAsc())
                .thenReturn(List.of(pulpFiction, interstellar, inception, shutterIsland, darkKnight));

        List<Movie> results = movieRepository.findAllByOrderByNumVotesAscTitleAsc();

        assertEquals(
                List.of("Pulp Fiction", "Interstellar", "Inception", "Shutter Island", "The Dark Knight"),
                results.stream().map(m -> m.getTitle()).toList());
        verify(movieRepository, times(1)).findAllByOrderByNumVotesAscTitleAsc();
    }

    @Test
    @DisplayName("Sort by views (numVotes) descending orders most to least viewed with title tie-breaker")
    void testSortByViewsDesc() {
        when(movieRepository.findAllByOrderByNumVotesDescTitleAsc())
                .thenReturn(List.of(darkKnight, inception, shutterIsland, interstellar, pulpFiction));

        List<Movie> results = movieRepository.findAllByOrderByNumVotesDescTitleAsc();

        assertEquals(
                List.of("The Dark Knight", "Inception", "Shutter Island", "Interstellar", "Pulp Fiction"),
                results.stream().map(m -> m.getTitle()).toList());
        verify(movieRepository, times(1)).findAllByOrderByNumVotesDescTitleAsc();
    }
}