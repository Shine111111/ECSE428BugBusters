package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.dto.WatchLaterResponse;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class WatchLaterService {

    // confirmation messages
    public static final String MOVIE_ADDED = "This movie was added to your Watch Later playlist.";
    public static final String FIRST_MOVIE_ADDED = "You have added your first movie to your Watch Later playlist.";

    // error messages
    public static final String MOVIE_ALREADY_ADDED = "This movie has already been added to the playlist.";
    public static final String MOVIE_NOT_EXIST = "This movie was not found.";
    public static final String LOGIN_REQUIRED = "You must be logged in to use Watch Later";
    public static final String USER_NOT_FOUND = "User not found";

    private final WatchLaterRepository watchLaterRepository; // TO BE ADDED BY HAMZA (CHECK WORDING)
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public WatchLaterService (WatchLaterRepository watchLaterRepository, MovieRepository movieRepository, UserRepository userRepository)
    {
        this(watchLaterRepository, movieRepository, userRepository, Clock.systemUTC());
    }

    public WatchLaterService(WatchLaterRepository watchLaterRepository, MovieRepository movieRepository, UserRepository userRepository, Clock clock)
    {
        this.watchLaterRepository = watchLaterRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public WatchLaterResponse addMovieToWatchLater(String userId, String movieId)
    {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(LOGIN_REQUIRED);
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(USER_NOT_FOUND);
        }
        if (movieId == null || movieId.isBlank() || !movieRepository.existsById(movieId)) {
            throw new IllegalArgumentException(MOVIE_NOT_EXIST);
        }
        if (watchLaterRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new IllegalArgumentException(MOVIE_ALREADY_ADDED);
        }

        boolean firstMovie = watchLaterRepository.countByUserId(userId) == 0;
        watchLaterRepository.save(new WatchLaterEntry(userId, movieId, Instant.now(clock)));

        String message = firstMovie ? FIRST_MOVIE_ADDED : MOVIE_ADDED;
        return new WatchLaterResponse(userId, movieId, message);
    }

    // Movie IDs in the user's playlist, newest first (a just-added movie is at index 0)
    @Transactional(readOnly = true)
    public List<String> getWatchLaterMovieIds(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(LOGIN_REQUIRED);
        }
        return watchLaterRepository.findByUserIdOrderByAddedAtDescIdDesc(userId).stream()
                .map(WatchLaterEntry::getMovieId)
                .toList();
    }

}
