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
        JOIN Role c ON c.movieId = m.movieId
        JOIN Person p ON p.personId = c.personId
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :personName, '%'))
        """)
    List<Movie> findMoviesByPersonNameContainingIgnoreCase(@Param("personName") String personName);

    @Query("""
        SELECT m
        FROM Movie m
        WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :query, '%'))
        """)
    List<Movie> searchMovies(@Param("query") String query);
}