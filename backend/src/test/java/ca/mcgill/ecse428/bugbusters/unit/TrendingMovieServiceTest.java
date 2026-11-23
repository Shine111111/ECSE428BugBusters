package ca.mcgill.ecse428.bugbusters.unit;

import ca.mcgill.ecse428.bugbusters.dto.TrendingMovieResponse;
import ca.mcgill.ecse428.bugbusters.dto.TrendingMoviesResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.TrendingMovieService;
import ca.mcgill.ecse428.bugbusters.service.trending.TrendingScoreCalculator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** SID0027: retrieval, eligibility, and missing-data coverage (TID037, TID042, TID043). */
@ExtendWith(MockitoExtension.class)
class TrendingMovieServiceTest {
    @Mock private MovieRepository repository;
    private TrendingMovieService service;

    @BeforeEach
    void setUp() {
        service = new TrendingMovieService(repository, new TrendingScoreCalculator());
    }

    private Movie movie(String id, String title, String rating, long votes, int comments, int views) {
        Movie movie = new Movie(id, title, "Drama", 2010, 120, new BigDecimal(rating), votes);
        movie.setCommentCount(comments);
        movie.setViewCount(views);
        return movie;
    }

    private List<String> titles(TrendingMoviesResponse response) {
        return response.movies().stream().map(TrendingMovieResponse::title).toList();
    }

    private void assertScore(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    @Test
    void retrievesAllFourSignalsAndRanksTheFeatureCatalog() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("django", "Django Unchained", "8.4", 520000, 3500, 700000),
                movie("hidden", "Hidden Gem", "9.4", 20000, 800, 100000),
                movie("pulp", "Pulp Fiction", "8.9", 480000, 4000, 750000),
                movie("inception", "Inception", "8.8", 600000, 4500, 850000),
                movie("dark", "The Dark Knight", "9.0", 700000, 5000, 900000),
                movie("interstellar", "Interstellar", "8.7", 550000, 4200, 800000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("The Dark Knight", "Inception", "Interstellar", "Pulp Fiction", "Django Unchained"),
                titles(response));
        TrendingMovieResponse first = response.movies().get(0);
        assertEquals("dark", first.movieId());
        assertEquals(0, new BigDecimal("9.0").compareTo(first.rating()));
        assertEquals(700000L, first.numberOfVotes());
        assertEquals(5000, first.commentCount());
        assertEquals(900000, first.viewCount());
        List<String> scores = List.of("1110", "1043", "984", "927", "871");
        for (int i = 0; i < scores.size(); i++) {
            assertScore(scores.get(i), response.movies().get(i).trendingScore());
        }
        assertNull(response.message());
        verify(repository).findByNumVotesGreaterThanEqual(30000L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void includesExactlyTheMinimumVotesAndExcludesOneVoteBelowIt() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("below", "Below", "10", 29999, 100000, 1000000),
                movie("at", "At threshold", "1", 30000, 0, 0)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("At threshold"), titles(response));
        assertScore("13", response.movies().get(0).trendingScore());
    }

    @Test   // test that erify several eligible/ineligible movies together
    void filtersMoviesUsingMinimumVoteThreshold() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("eligible1", "Eligible One", "8.0", 30000, 100, 1000),
                movie("eligible2", "Eligible Two", "7.5", 50000, 200, 2000),
                movie("below", "Below Threshold", "9.5", 29999, 5000, 1000000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(2, response.movies().size());
        assertTrue(titles(response).contains("Eligible One"));
        assertTrue(titles(response).contains("Eligible Two"));
        assertFalse(titles(response).contains("Below Threshold"));

        assertTrue(response.movies().stream()
                .allMatch(movie -> movie.numberOfVotes() >= 30000));
    }

    @Test   // test that verify several ineligible movies together
    void excludesAllMoviesBelowMinimumVoteThreshold() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("below1", "Below One", "9.5", 10000, 5000, 1000000),
                movie("below2", "Below Two", "9.0", 29999, 4000, 900000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertTrue(response.movies().isEmpty());
        assertEquals("No trending movies available", response.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {"rating", "votes", "comments", "views"})
    void excludesEachMissingSignalWithoutLosingValidMovies(String missingSignal) {
        Movie incomplete = movie("incomplete", "Incomplete", "9", 500000, 5000, 900000);
        switch (missingSignal) {
            case "rating" -> incomplete.setRatings(null);
            case "votes" -> incomplete.setNumVotes(null);
            case "comments" -> incomplete.setCommentCount(null);
            case "views" -> incomplete.setViewCount(null);
            default -> throw new AssertionError(missingSignal);
        }
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                incomplete, movie("valid", "Valid", "8", 30000, 0, 0)));

        assertEquals(List.of("Valid"), titles(service.getTrendingMovies()));
    }

    @Test
    void acceptsExplicitZeroRatingCommentsAndViews() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("zero", "Zero activity", "0", 30000, 0, 0)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("Zero activity"), titles(response));
        assertScore("3", response.movies().get(0).trendingScore());
    }

    @Test
    void skipsNullMovieWithoutFailingTheRequest() {
        List<Movie> movies = new ArrayList<>();
        movies.add(null);
        movies.add(movie("valid", "Valid", "8", 30000, 0, 0));

        when(repository.findByNumVotesGreaterThanEqual(30000L))
                .thenReturn(movies);

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("Valid"), titles(response));
        assertNull(response.message());
    }

    @Test
    void skipsMultipleInvalidMoviesAndStillReturnsValidMovies() {
        Movie missingRating =
                movie("missing-rating", "Missing Rating", "9", 500000, 5000, 900000);
        missingRating.setRatings(null);

        Movie missingComments =
                movie("missing-comments", "Missing Comments", "9", 500000, 5000, 900000);
        missingComments.setCommentCount(null);

        Movie negativeViews =
                movie("negative-views", "Negative Views", "9", 500000, 5000, 900000);
        negativeViews.setViewCount(-1);

        Movie valid =
                movie("valid", "Valid", "8", 30000, 0, 0);

        List<Movie> movies = new ArrayList<>();
        movies.add(missingRating);
        movies.add(missingComments);
        movies.add(negativeViews);
        movies.add(valid);

        when(repository.findByNumVotesGreaterThanEqual(30000L))
                .thenReturn(movies);

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("Valid"), titles(response));
        assertNull(response.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {"rating", "votes", "comments", "views"})
    void skipsNegativeSignalsWithoutFailingTheRequest(String invalidSignal) {
        Movie invalid = movie("invalid", "Invalid", "9", 500000, 5000, 900000);
        switch (invalidSignal) {
            case "rating" -> invalid.setRatings(new BigDecimal("-1"));
            case "votes" -> invalid.setNumVotes(-1L);
            case "comments" -> invalid.setCommentCount(-1);
            case "views" -> invalid.setViewCount(-1);
            default -> throw new AssertionError(invalidSignal);
        }
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                invalid, movie("valid", "Valid", "8", 30000, 0, 0)));

        assertEquals(List.of("Valid"), titles(service.getTrendingMovies()));
    }

    @Test
    void returnsInformativeEmptyResponseForAnEmptyCatalog() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of());

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertTrue(response.movies().isEmpty());
        assertEquals("No trending movies available", response.message());
    }

    @Test
    void returnsInformativeEmptyResponseWhenAllMoviesAreIneligible() {
        Movie missingViews = movie("missing", "Missing", "9", 500000, 5000, 900000);
        missingViews.setViewCount(null);
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                missingViews, movie("below", "Below", "9", 29999, 5000, 900000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertTrue(response.movies().isEmpty());
        assertEquals("No trending movies available", response.message());
    }

    @Test
    void commentsAndViewsChangeRankingWhenRatingAndVotesAreEqual() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("aurora", "Aurora", "8.5", 400000, 2000, 300000),
                movie("apollo", "Apollo", "8.5", 400000, 5000, 700000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("Apollo", "Aurora"), titles(response));
        assertScore("875", response.movies().get(0).trendingScore());
        assertScore("445", response.movies().get(1).trendingScore());
    }

    @Test
    void resolvesScoreTiesByVotesThenTitleThenMovieId() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("comet", "Comet", "8", 90000, 1100, 100000),
                movie("beacon", "Beacon", "8", 90000, 1100, 100000),
                movie("apollo", "Apollo", "8", 90000, 1100, 100000),
                movie("aurora", "Aurora", "8", 100000, 1000, 100000),
                movie("a-2", "Duplicate", "8", 90000, 1100, 100000),
                movie("a-1", "duplicate", "8", 90000, 1100, 100000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("aurora", "apollo", "beacon", "comet", "a-1", "a-2"),
                response.movies().stream().map(TrendingMovieResponse::movieId).toList());
        response.movies().forEach(result -> assertScore("200", result.trendingScore()));
    }

    @Test //Test score ordering
    void ranksMoviesByTrendingScoreDescending() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("low", "Lower Score", "8.0", 30000, 0, 0),
                movie("high", "Higher Score", "9.0", 50000, 1000, 10000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(List.of("Higher Score", "Lower Score"), titles(response));

        assertTrue(
                response.movies().get(0).trendingScore()
                        .compareTo(response.movies().get(1).trendingScore()) > 0);
    }

    @Test // Test vote-count tie breaker
    void breaksEqualTrendingScoresUsingNumberOfVotesDescending() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("apollo", "Apollo", "8.0", 90000, 1100, 100000),
                movie("aurora", "Aurora", "8.0", 100000, 1000, 100000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertScore("200", response.movies().get(0).trendingScore());
        assertScore("200", response.movies().get(1).trendingScore());

        assertEquals("Aurora", response.movies().get(0).title());
        assertEquals("Apollo", response.movies().get(1).title());
    }

    @Test   //Test title tie breaker
    void breaksScoreAndVoteTiesUsingTitleAlphabetically() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("comet", "Comet", "8.0", 90000, 1100, 100000),
                movie("apollo", "Apollo", "8.0", 90000, 1100, 100000),
                movie("beacon", "Beacon", "8.0", 90000, 1100, 100000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(
                List.of("Apollo", "Beacon", "Comet"),
                titles(response));

        response.movies().forEach(movie ->
                assertScore("200", movie.trendingScore()));
    }

    @Test   //Test final movie-ID tie breaker
    void breaksCompleteTiesUsingMovieId() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("movie-3", "Duplicate", "8.0", 90000, 1100, 100000),
                movie("movie-1", "Duplicate", "8.0", 90000, 1100, 100000),
                movie("movie-2", "Duplicate", "8.0", 90000, 1100, 100000)));

        TrendingMoviesResponse response = service.getTrendingMovies();

        assertEquals(
                List.of("movie-1", "movie-2", "movie-3"),
                response.movies().stream()
                        .map(TrendingMovieResponse::movieId)
                        .toList());
    }

    @Test
    void retrievesFreshPopularityDataAndRecalculatesEachRequest() {
        Movie darkKnight = movie("dark", "The Dark Knight", "9", 700000, 5000, 900000);
        Movie pulpFiction = movie("pulp", "Pulp Fiction", "8.9", 480000, 4000, 750000);
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(darkKnight, pulpFiction));
        TrendingMoviesResponse before = service.getTrendingMovies();

        pulpFiction.setNumVotes(780000L);
        pulpFiction.setViewCount(1050000);
        TrendingMoviesResponse after = service.getTrendingMovies();

        assertEquals(List.of("The Dark Knight", "Pulp Fiction"), titles(before));
        assertScore("927", before.movies().get(1).trendingScore());
        assertEquals(List.of("Pulp Fiction", "The Dark Knight"), titles(after));
        assertScore("1257", after.movies().get(0).trendingScore());
        assertEquals(480000L, before.movies().get(1).numberOfVotes());
        verify(repository, times(2)).findByNumVotesGreaterThanEqual(30000L);
    }

    @Test
    void preservesVoteCountsAboveTheIntegerRange() {
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(List.of(
                movie("large", "Large", "8", 3000000000L, 0, 0)));

        TrendingMovieResponse response = service.getTrendingMovies().movies().get(0);

        assertEquals(3000000000L, response.numberOfVotes());
        assertScore("300080", response.trendingScore());
    }

    @Test
    void doesNotReorderTheRepositoryResult() {
        Movie lower = movie("low", "Lower", "8", 30000, 0, 0);
        Movie higher = movie("high", "Higher", "8", 40000, 0, 0);
        List<Movie> input = new ArrayList<>(List.of(lower, higher));
        when(repository.findByNumVotesGreaterThanEqual(30000L)).thenReturn(input);

        assertEquals(List.of("Higher", "Lower"), titles(service.getTrendingMovies()));
        assertEquals(List.of(lower, higher), input);
    }
}
