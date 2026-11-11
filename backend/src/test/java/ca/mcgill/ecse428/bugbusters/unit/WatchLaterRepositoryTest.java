package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.WatchLaterEntry;
import ca.mcgill.ecse428.bugbusters.repository.WatchLaterRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

// TID025: unit tests for the Watch Later repository (save and lookup)
// Runs against an in-memory H2 database (see src/test/resources/application.properties)
@DataJpaTest
class WatchLaterRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-11-01T10:00:00Z");

    @Autowired
    private WatchLaterRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    // saves and flushes, then clears the persistence context so reads come from the database
    private WatchLaterEntry persist(WatchLaterEntry entry) {
        WatchLaterEntry saved = repository.saveAndFlush(entry);
        entityManager.clear();
        return saved;
    }

    @Test
    void savedEntryGetsGeneratedIdAndCanBeFoundById() {
        WatchLaterEntry saved = persist(new WatchLaterEntry("0001", "tt0110912", T0));
        assertNotNull(saved.getId());
        WatchLaterEntry found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("0001", found.getUserId());
        assertEquals("tt0110912", found.getMovieId());
        assertEquals(T0, found.getAddedAt());
    }

    @Test
    void savedEntryCanBeFoundByUserAndMovie() {
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        WatchLaterEntry found = repository.findByUserIdAndMovieId("0001", "tt0110912").orElseThrow();
        assertEquals("0001", found.getUserId());
        assertEquals("tt0110912", found.getMovieId());
        assertTrue(repository.existsByUserIdAndMovieId("0001", "tt0110912"));
    }

    @Test
    void lookupOfMissingPairReturnsEmpty() {
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        assertTrue(repository.findByUserIdAndMovieId("0001", "tt0816692").isEmpty());
        assertTrue(repository.findByUserIdAndMovieId("0002", "tt0110912").isEmpty());
        assertFalse(repository.existsByUserIdAndMovieId("0001", "tt0816692"));
        assertFalse(repository.existsByUserIdAndMovieId("0002", "tt0110912"));
    }

    @Test
    void emptyPlaylistHasNoEntries() {
        assertEquals(0, repository.countByUserId("0001"));
        assertTrue(repository.findByUserIdOrderByAddedAtDescIdDesc("0001").isEmpty());
    }

    @Test
    void playlistIsReturnedNewestFirst() {
        persist(new WatchLaterEntry("0001", "tt0468569", T0.plusSeconds(20)));
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        persist(new WatchLaterEntry("0001", "tt0816692", T0.plusSeconds(10)));
        List<String> movieIds = repository.findByUserIdOrderByAddedAtDescIdDesc("0001").stream()
                .map(WatchLaterEntry::getMovieId).toList();
        assertEquals(List.of("tt0468569", "tt0816692", "tt0110912"), movieIds);
        assertEquals(3, repository.countByUserId("0001"));
    }

    @Test
    void entriesAddedAtSameInstantAreReturnedNewestSavedFirst() {
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        persist(new WatchLaterEntry("0001", "tt0816692", T0));
        List<String> movieIds = repository.findByUserIdOrderByAddedAtDescIdDesc("0001").stream()
                .map(WatchLaterEntry::getMovieId).toList();
        assertEquals(List.of("tt0816692", "tt0110912"), movieIds);
    }

    @Test
    void playlistsAreSeparatePerUser() {
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        persist(new WatchLaterEntry("0001", "tt0816692", T0));
        persist(new WatchLaterEntry("0002", "tt0110912", T0));
        assertEquals(2, repository.countByUserId("0001"));
        assertEquals(1, repository.countByUserId("0002"));
        assertEquals(List.of("tt0110912"), repository.findByUserIdOrderByAddedAtDescIdDesc("0002").stream()
                .map(WatchLaterEntry::getMovieId).toList());
    }

    @Test
    void sameMovieCanBeSavedByDifferentUsers() {
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        persist(new WatchLaterEntry("0002", "tt0110912", T0));
        assertEquals(2, repository.count());
    }

    @Test
    void rejectsSameMovieTwiceForSameUser() {
        persist(new WatchLaterEntry("0001", "tt0110912", T0));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new WatchLaterEntry("0001", "tt0110912", T0.plusSeconds(5))));
        entityManager.clear();
        assertEquals(1, repository.countByUserId("0001"));
    }

    @Test
    void rejectsNullUserId() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new WatchLaterEntry(null, "tt0110912", T0)));
    }

    @Test
    void rejectsNullMovieId() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new WatchLaterEntry("0001", null, T0)));
    }

    @Test
    void deletingEntryRemovesItFromPlaylist() {
        WatchLaterEntry saved = persist(new WatchLaterEntry("0001", "tt0110912", T0));
        repository.deleteById(saved.getId());
        repository.flush();
        assertFalse(repository.existsByUserIdAndMovieId("0001", "tt0110912"));
        assertEquals(0, repository.countByUserId("0001"));
    }

    @Test
    void defaultConstructorSetsAddedAtToNow() {
        Instant before = Instant.now();
        WatchLaterEntry entry = new WatchLaterEntry("0001", "tt0110912");
        assertFalse(entry.getAddedAt().isBefore(before));
        assertFalse(entry.getAddedAt().isAfter(Instant.now()));
    }
}
