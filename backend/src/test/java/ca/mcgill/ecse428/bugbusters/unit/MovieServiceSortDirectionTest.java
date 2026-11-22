package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class MovieServiceSortDirectionTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private List<Movie> catalog;

    @BeforeEach
    void setUp() {
        // Movies from ID_017_Sort_Movies.feature
        catalog = List.of(
                new Movie("m1", "Inception", "Sci-Fi", 2010, 148, new BigDecimal("8.8"), 600000L),
                new Movie("m2", "Interstellar", "Sci-Fi", 2014, 169, new BigDecimal("8.7"), 550000L),
                new Movie("m3", "The Dark Knight", "Action", 2008, 152, new BigDecimal("9.0"), 700000L),
                new Movie("m4", "Pulp Fiction", "Crime", 1994, 154, new BigDecimal("8.9"), 480000L),
                new Movie("m5", "Django Unchained", "Western", 2012, 165, new BigDecimal("8.4"), 520000L)
        );
    }

    // ---------------------------------------------------------------------
    // sortMovies (subset of results)
    // ---------------------------------------------------------------------

    @ParameterizedTest(name = "{0} {1} -> {2} {3}")
    @CsvSource({
            "title,        ascending,  title,     ASC",
            "title,        descending, title,     DESC",
            "rating,       ascending,  ratings,   ASC",
            "rating,       descending, ratings,   DESC",
            "views,        ascending,  numVotes,  ASC",
            "views,        descending, numVotes,  DESC",
            "release_year, ascending,  startYear, ASC",
            "release_year, descending, startYear, DESC"
    })
    @DisplayName("sortMovies applies the requested direction to the primary sort property")
    void testSortMoviesAppliesDirection(String criterion, String direction,
                                        String expectedProperty, Sort.Direction expectedDirection) {
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(movieRepository.findByMovieIdIn(anyList(), sortCaptor.capture())).thenReturn(catalog);

        movieService.sortMovies(catalog, criterion, direction);

        Sort.Order primary = sortCaptor.getValue().iterator().next();
        assertEquals(expectedProperty, primary.getProperty());
        assertEquals(expectedDirection, primary.getDirection());
    }

    @ParameterizedTest(name = "{0} descending keeps title ascending as tie-break")
    @ValueSource(strings = {"rating", "views", "release_year"})
    @DisplayName("Descending sort still breaks ties by title ascending")
    void testDescendingTieBreakIsTitleAscending(String criterion) {
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(movieRepository.findByMovieIdIn(anyList(), sortCaptor.capture())).thenReturn(catalog);

        movieService.sortMovies(catalog, criterion, "descending");

        Iterator<Sort.Order> orders = sortCaptor.getValue().iterator();
        assertEquals(Sort.Direction.DESC, orders.next().getDirection());
        Sort.Order tieBreak = orders.next();
        assertEquals("title", tieBreak.getProperty());
        assertEquals(Sort.Direction.ASC, tieBreak.getDirection());
    }

    @Test
    @DisplayName("Short aliases 'asc' and 'desc' behave like the full words")
    void testShortAliases() {
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(movieRepository.findByMovieIdIn(anyList(), sortCaptor.capture())).thenReturn(catalog);

        movieService.sortMovies(catalog, "views", "asc");
        movieService.sortMovies(catalog, "views", "DESC");

        List<Sort> sorts = sortCaptor.getAllValues();
        assertEquals(Sort.Direction.ASC, sorts.get(0).getOrderFor("numVotes").getDirection());
        assertEquals(Sort.Direction.DESC, sorts.get(1).getOrderFor("numVotes").getDirection());
    }

    @ParameterizedTest(name = "sortMovies rejects direction \"{0}\"")
    @NullSource
    @ValueSource(strings = {"", "sideways", "up"})
    @DisplayName("sortMovies rejects missing or unknown directions without querying")
    void testSortMoviesInvalidDirection(String direction) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> movieService.sortMovies(catalog, "title", direction)
        );
        assertEquals("Invalid sort direction", exception.getMessage());
        verifyNoInteractions(movieRepository);
    }

    // ---------------------------------------------------------------------
    // sortAllMovies (whole catalog)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("sortAllMovies rating ascending uses only the ascending query")
    void testSortAllRatingAscending() {
        when(movieRepository.findAllByOrderByRatingsAscTitleAsc()).thenReturn(catalog);

        movieService.sortAllMovies("rating", "ascending");

        verify(movieRepository).findAllByOrderByRatingsAscTitleAsc();
        verify(movieRepository, never()).findAllByOrderByRatingsDescTitleAsc();
    }

    @Test
    @DisplayName("sortAllMovies rating descending uses only the descending query")
    void testSortAllRatingDescending() {
        when(movieRepository.findAllByOrderByRatingsDescTitleAsc()).thenReturn(catalog);

        movieService.sortAllMovies("rating", "descending");

        verify(movieRepository).findAllByOrderByRatingsDescTitleAsc();
        verify(movieRepository, never()).findAllByOrderByRatingsAscTitleAsc();
    }

    @Test
    @DisplayName("sortAllMovies release_year ascending uses only the ascending query")
    void testSortAllYearAscending() {
        when(movieRepository.findAllByOrderByStartYearAscTitleAsc()).thenReturn(catalog);

        movieService.sortAllMovies("release_year", "asc");

        verify(movieRepository).findAllByOrderByStartYearAscTitleAsc();
        verify(movieRepository, never()).findAllByOrderByStartYearDescTitleAsc();
    }

    @Test
    @DisplayName("sortAllMovies release_year descending uses only the descending query")
    void testSortAllYearDescending() {
        when(movieRepository.findAllByOrderByStartYearDescTitleAsc()).thenReturn(catalog);

        movieService.sortAllMovies("release_year", "desc");

        verify(movieRepository).findAllByOrderByStartYearDescTitleAsc();
        verify(movieRepository, never()).findAllByOrderByStartYearAscTitleAsc();
    }

    @ParameterizedTest(name = "sortAllMovies title {0} -> title {1}")
    @CsvSource({
            "ascending,  ASC",
            "descending, DESC"
    })
    @DisplayName("sortAllMovies title passes the requested direction to findAll(Sort)")
    void testSortAllTitleFindAllDirection(String direction, Sort.Direction expectedDirection) {
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(movieRepository.findAll(sortCaptor.capture())).thenReturn(catalog);

        movieService.sortAllMovies("title", direction);

        Sort.Order order = sortCaptor.getValue().getOrderFor("title");
        assertNotNull(order);
        assertEquals(expectedDirection, order.getDirection());
    }

    @Test
    @DisplayName("sortAllMovies views ascending uses only the ascending query")
    void testSortAllViewsAscending() {
        when(movieRepository.findAllByOrderByNumVotesAscTitleAsc()).thenReturn(catalog);

        movieService.sortAllMovies("views", "ascending");

        verify(movieRepository).findAllByOrderByNumVotesAscTitleAsc();
        verify(movieRepository, never()).findAllByOrderByNumVotesDescTitleAsc();
    }

    @Test
    @DisplayName("sortAllMovies views descending uses only the descending query")
    void testSortAllViewsDescending() {
        when(movieRepository.findAllByOrderByNumVotesDescTitleAsc()).thenReturn(catalog);

        movieService.sortAllMovies("views", "descending");

        verify(movieRepository).findAllByOrderByNumVotesDescTitleAsc();
        verify(movieRepository, never()).findAllByOrderByNumVotesAscTitleAsc();
    }

    @ParameterizedTest(name = "sortAllMovies rejects direction \"{0}\"")
    @NullSource
    @ValueSource(strings = {"", "sideways", "up"})
    @DisplayName("sortAllMovies rejects missing or unknown directions without querying")
    void testSortAllMoviesInvalidDirection(String direction) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> movieService.sortAllMovies("rating", direction)
        );
        assertEquals("Invalid sort direction", exception.getMessage());
        verifyNoInteractions(movieRepository);
    }
}
