package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, String> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByGenreContainingIgnoreCase(String genre);

    List<Movie> findByStartYear(Integer startYear);

    List<Movie> findByRuntime(Integer runtime);

    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        JOIN Role r ON r.movieId = m.movieId
        JOIN Person p ON p.personId = r.personId
        WHERE LOWER(r.role) IN ('actor', 'actress')
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :actorName, '%'))
        """)
    List<Movie> findMoviesByActorNameContainingIgnoreCase(@Param("actorName") String actorName);

    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        JOIN Role r ON r.movieId = m.movieId
        JOIN Person p ON p.personId = r.personId
        WHERE LOWER(r.role) = 'director'
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :directorName, '%'))
        """)
    List<Movie> findMoviesByDirectorNameContainingIgnoreCase(@Param("directorName") String directorName);

    @Query("""
        SELECT m
        FROM Movie m
        WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    List<Movie> searchMovies(@Param("query") String query);
}