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

    private Movie pulpFiction;   // 1994, 8.9
    private Movie darkKnight;    // 2008, 9.0
    private Movie inception;     // 2010, 8.8
    private Movie interstellar;  // 2014, 8.7

    @BeforeEach
    void setUp() {
        pulpFiction = new Movie("m1", "Pulp Fiction", "Crime", 1994, 154, new BigDecimal("8.9"), 480000L);
        darkKnight = new Movie("m2", "The Dark Knight", "Action", 2008, 152, new BigDecimal("9.0"), 700000L);
        inception = new Movie("m3", "Inception", "Sci-Fi", 2010, 148, new BigDecimal("8.8"), 600000L);
        interstellar = new Movie("m4", "Interstellar", "Sci-Fi", 2014, 169, new BigDecimal("8.7"), 550000L);
    }

    @Test
    @DisplayName("Sort by start year ascending orders oldest to newest")
    void testSortByStartYearAsc() {
        when(movieRepository.findAllByOrderByStartYearAsc())
                .thenReturn(List.of(pulpFiction, darkKnight, inception, interstellar));

        List<Movie> results = movieRepository.findAllByOrderByStartYearAsc();

        assertEquals(List.of("Pulp Fiction", "The Dark Knight", "Inception", "Interstellar"),
                results.stream().map(Movie::getTitle).toList());
        verify(movieRepository, times(1)).findAllByOrderByStartYearAsc();
    }

    @Test
    @DisplayName("Sort by start year descending orders newest to oldest")
    void testSortByStartYearDesc() {
        when(movieRepository.findAllByOrderByStartYearDesc())
                .thenReturn(List.of(interstellar, inception, darkKnight, pulpFiction));

        List<Movie> results = movieRepository.findAllByOrderByStartYearDesc();

        assertEquals(List.of("Interstellar", "Inception", "The Dark Knight", "Pulp Fiction"),
                results.stream().map(Movie::getTitle).toList());
        verify(movieRepository, times(1)).findAllByOrderByStartYearDesc();
    }

    @Test
    @DisplayName("Sort by ratings ascending orders lowest to highest")
    void testSortByRatingsAsc() {
        when(movieRepository.findAllByOrderByRatingsAsc())
                .thenReturn(List.of(interstellar, inception, pulpFiction, darkKnight));

        List<Movie> results = movieRepository.findAllByOrderByRatingsAsc();

        assertEquals(List.of("Interstellar", "Inception", "Pulp Fiction", "The Dark Knight"),
                results.stream().map(Movie::getTitle).toList());
        verify(movieRepository, times(1)).findAllByOrderByRatingsAsc();
    }

    @Test
    @DisplayName("Sort by ratings descending orders highest to lowest")
    void testSortByRatingsDesc() {
        when(movieRepository.findAllByOrderByRatingsDesc())
                .thenReturn(List.of(darkKnight, pulpFiction, inception, interstellar));

        List<Movie> results = movieRepository.findAllByOrderByRatingsDesc();

        assertEquals(List.of("The Dark Knight", "Pulp Fiction", "Inception", "Interstellar"),
                results.stream().map(Movie::getTitle).toList());
        verify(movieRepository, times(1)).findAllByOrderByRatingsDesc();
    }
}