package ca.mcgill.ecse428.bugbusters.controller;

import ca.mcgill.ecse428.bugbusters.dto.WatchLaterResponse;
import ca.mcgill.ecse428.bugbusters.service.WatchLaterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import ca.mcgill.ecse428.bugbusters.dto.ErrorResponse;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/watch-later")
public class WatchLaterController {

    private final WatchLaterService watchLaterService;

    public WatchLaterController(WatchLaterService watchLaterService)
    {
        this.watchLaterService = watchLaterService;
    }

    // 201 with the confirmation message, 409 if already added, 404 if the movie or user doesn't exist
    @PostMapping("/{movieId}")
    public ResponseEntity<WatchLaterResponse> addMovie(@PathVariable String userId,
                                                       @PathVariable String movieId) {
        WatchLaterResponse response = watchLaterService.addMovieToWatchLater(userId, movieId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<String> getMovieIds(@PathVariable String userId) {
        return watchLaterService.getWatchLaterMovieIds(userId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleRejected(IllegalArgumentException e) {
        HttpStatus status = switch (e.getMessage()) {
            case WatchLaterService.MOVIE_ALREADY_ADDED -> HttpStatus.CONFLICT;
            case WatchLaterService.MOVIE_NOT_EXIST, WatchLaterService.USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case WatchLaterService.LOGIN_REQUIRED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
    }

    // Two requests adding the same movie at the same time can both pass the duplicate check;
    // the unique constraint on (user_id, movie_id) then rejects the second one
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateInsert(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(WatchLaterService.MOVIE_ALREADY_ADDED));
    }
}
