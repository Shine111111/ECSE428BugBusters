package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.WatchLaterEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// watch_later table: one row per (user_id, movie_id), unique per pair
public interface WatchLaterRepository extends JpaRepository<WatchLaterEntry, Long> {

    // the user's playlist, newest addition first (ties broken by insertion order, newest first)
    List<WatchLaterEntry> findByUserIdOrderByAddedAtDescIdDesc(String userId);

    Optional<WatchLaterEntry> findByUserIdAndMovieId(String userId, String movieId);

    boolean existsByUserIdAndMovieId(String userId, String movieId);

    long countByUserId(String userId);
}
