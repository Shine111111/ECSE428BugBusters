package ca.mcgill.ecse428.bugbusters.unit;

import ca.mcgill.ecse428.bugbusters.model.*;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import ca.mcgill.ecse428.bugbusters.service.recommendation.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.config.import=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class MovieRecommendationIntegrationTest {
    @Autowired private MovieRepository movies;
    @Autowired private TestEntityManager entityManager;

    @Test void persistedCatalogMoviesSupportSearchAndRecommendationScoring() {
        movies.saveAndFlush(new Movie("catalog-1", "Arrival", "Drama, Sci-Fi", 2016,
                116, new BigDecimal("4.6"), 900L));
        entityManager.clear();

        Movie loaded = new MovieService(movies).searchMoviesByTitle("Arrival").get(0);
        assertEquals("catalog-1", loaded.getId());
        assertEquals(Set.of("Drama", "Sci-Fi"), loaded.getGenres());
        assertEquals(30, new PreferenceScorer().score(loaded,
                new UserPreferences(Set.of("Sci-Fi"), Set.of(), Set.of())));
        assertEquals(18.2, new PopularityScorer().score(loaded), 0.00001);
        assertTrue(loaded.getActors().isEmpty());
        assertNull(loaded.getDirector());
    }

    @Test void missingDirectorMetadataDoesNotCreateHistoryMatch() {
        Movie candidate = new Movie("a", "A", null, null, null, null, null);
        Movie rated = new Movie("b", "B", null, null, null, null, null);
        assertEquals(0, new RatingHistoryScorer().score(candidate,
                List.of(new Review("u", "b", 5, null)), Map.of("b", rated)));
        assertEquals(0, new PopularityScorer().score(candidate));
    }

    @Test void recommendationConstructorPreservesCatalogIdentityAndMetadata() {
        movies.saveAndFlush(new Movie("rec-1", "Dune", Set.of("Sci-Fi"), Set.of("Actor"),
                "Director", 4.5, 120, 3, 10));
        entityManager.clear();
        Movie loaded = new MovieService(movies).findMovieById("rec-1");
        assertEquals("rec-1", loaded.getId());
        assertEquals(Set.of("Sci-Fi"), loaded.getGenres());
        assertEquals(4.5, loaded.getAverageRating());
        assertEquals(120, loaded.getRatingCount());
    }
}
