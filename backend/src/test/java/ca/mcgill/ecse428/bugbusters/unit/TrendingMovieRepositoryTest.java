package ca.mcgill.ecse428.bugbusters.unit;

import ca.mcgill.ecse428.bugbusters.dto.TrendingMovieResponse;
import ca.mcgill.ecse428.bugbusters.dto.TrendingMoviesResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.TrendingMovieService;
import ca.mcgill.ecse428.bugbusters.service.trending.TrendingScoreCalculator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

/** TID042: exercises actual persisted trending data instead of mocked repository results. */
@DataJpaTest
class TrendingMovieRepositoryTest {
    @Autowired private MovieRepository repository;
    @Autowired private TestEntityManager entityManager;

    private Movie movie(String id, long votes, Integer comments, Integer views) {
        Movie movie = new Movie(id, id, "Drama", 2010, 120, new BigDecimal("9.0"), votes);
        movie.setCommentCount(comments);
        movie.setViewCount(views);
        return movie;
    }

    private void persistAndClear(Movie... movies) {
        repository.saveAllAndFlush(List.of(movies));
        entityManager.clear();
    }

    private TrendingMovieService service() {
        return new TrendingMovieService(repository, new TrendingScoreCalculator());
    }

    @Test
    void retrievesRatingVotesCommentsAndViewsAfterDatabaseReload() {
        persistAndClear(movie("The Dark Knight", 700000L, 5000, 900000));

        TrendingMovieResponse response = service().getTrendingMovies().movies().get(0);

        assertEquals("The Dark Knight", response.movieId());
        assertEquals("The Dark Knight", response.title());
        assertEquals(0, new BigDecimal("9.0").compareTo(response.rating()));
        assertEquals(700000L, response.numberOfVotes());
        assertEquals(5000, response.commentCount());
        assertEquals(900000, response.viewCount());
        assertEquals(0, new BigDecimal("1110").compareTo(response.trendingScore()));
    }

    @Test
    void repositoryIncludesTheThresholdAndExcludesLowerAndMissingVotes() {
        Movie noVotes = movie("missing", 30000L, 0, 0);
        noVotes.setNumVotes(null);
        persistAndClear(movie("below", 29999L, 0, 0), movie("exact", 30000L, 0, 0),
                movie("above", 30001L, 0, 0), noVotes);

        List<String> ids = repository.findByNumVotesGreaterThanEqual(30000L).stream()
                .map(Movie::getMovieId).sorted().toList();

        assertEquals(List.of("above", "exact"), ids);
    }

    @Test
    void preservesMissingMetricsAndDistinguishesThemFromExplicitZeros() {
        persistAndClear(movie("missing-comments", 30000L, null, 0),
                movie("missing-views", 30000L, 0, null), movie("zeros", 30000L, 0, 0));

        assertFalse(repository.findById("missing-comments").orElseThrow().hasTrendingData());
        assertFalse(repository.findById("missing-views").orElseThrow().hasTrendingData());
        assertTrue(repository.findById("zeros").orElseThrow().hasTrendingData());
        TrendingMoviesResponse response = service().getTrendingMovies();
        assertEquals(List.of("zeros"), response.movies().stream().map(TrendingMovieResponse::movieId).toList());
    }

    @Test
    void persistedPopularityUpdatesAreUsedOnTheNextRead() {
        persistAndClear(movie("updated", 30000L, 0, 0));
        TrendingMovieResponse before = service().getTrendingMovies().movies().get(0);
        Movie updated = repository.findById("updated").orElseThrow();
        updated.setRatings(new BigDecimal("8.5"));
        updated.setNumVotes(40000L);
        updated.setCommentCount(100);
        updated.setViewCount(1000);
        persistAndClear(updated);

        TrendingMovieResponse after = service().getTrendingMovies().movies().get(0);

        assertEquals(0, new BigDecimal("93").compareTo(before.trendingScore()));
        assertEquals(0, new BigDecimal("91").compareTo(after.trendingScore()));
        assertEquals(40000L, after.numberOfVotes());
        assertEquals(100, after.commentCount());
        assertEquals(1000, after.viewCount());
    }

    @Test
    void emptyDatabaseReturnsTheInformativeEmptyResponse() {
        TrendingMoviesResponse response = service().getTrendingMovies();

        assertTrue(response.movies().isEmpty());
        assertEquals("No trending movies available", response.message());
    }
}
