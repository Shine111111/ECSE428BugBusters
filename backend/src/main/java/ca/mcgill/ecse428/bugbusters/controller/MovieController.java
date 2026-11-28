package ca.mcgill.ecse428.bugbusters.controller;

import ca.mcgill.ecse428.bugbusters.dto.ErrorResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Searches movies and, when {@code sortBy} is given, sorts the results (ID017).
     *
     * @param sortBy    optional: title, rating, views or release_year
     * @param direction ascending/asc or descending/desc; defaults to ascending, ignored without sortBy
     */
    @GetMapping("/search")
    public List<Movie> search(@RequestParam(required = false, defaultValue = "") String criterion,
                              @RequestParam String query,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false, defaultValue = "ascending") String direction) {
        List<Movie> results = movieService.searchMovies(criterion, query);
        if (sortBy == null || sortBy.isBlank()) {
            return results;
        }
        return movieService.sortMovies(results, sortBy, direction);
    }

    // Invalid query, sort criterion or direction, or nothing to sort -> 400 with the reason
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }
}
