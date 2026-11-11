package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.WatchLater;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// watch_later table: one row per (user_id, movie_id), unique per pair
public interface WatchLaterRepository extends JpaRepository<WatchLater, Long> {

    // the user's playlist, oldest addition first
    List<WatchLater> findByUserIdOrderByAddedAtAscIdAsc(String userId);

    Optional<WatchLater> findByUserIdAndMovieId(String userId, String movieId);

    boolean existsByUserIdAndMovieId(String userId, String movieId);

    long countByUserId(String userId);
}
