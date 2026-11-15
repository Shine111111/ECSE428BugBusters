package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.dto.TrendingMovieResponse;
import ca.mcgill.ecse428.bugbusters.dto.TrendingMoviesResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.trending.TrendingScoreCalculator;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrendingMovieService {
    public static final long MINIMUM_VOTES = 30_000L;
    public static final String NO_TRENDING_MOVIES_MESSAGE = "No trending movies available";

    private static final Comparator<TrendingMovieResponse> TRENDING_ORDER = Comparator
            .comparing(TrendingMovieResponse::trendingScore, Comparator.reverseOrder())
            .thenComparing(Comparator.comparingLong(TrendingMovieResponse::numberOfVotes).reversed())
            .thenComparing(TrendingMovieResponse::title,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(TrendingMovieResponse::movieId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final MovieRepository movieRepository;
    private final TrendingScoreCalculator scoreCalculator;

    public TrendingMovieService(MovieRepository movieRepository, TrendingScoreCalculator scoreCalculator) {
        this.movieRepository = movieRepository;
        this.scoreCalculator = scoreCalculator;
    }

    @Transactional(readOnly = true)
    public TrendingMoviesResponse getTrendingMovies() {
        List<TrendingMovieResponse> movies = movieRepository.findByNumVotesGreaterThanEqual(MINIMUM_VOTES)
                .stream()
                .filter(this::isEligible)
                .map(this::toResponse)
                .sorted(TRENDING_ORDER)
                .toList();
        return new TrendingMoviesResponse(movies, movies.isEmpty() ? NO_TRENDING_MOVIES_MESSAGE : null);
    }

    private boolean isEligible(Movie movie) {
        return movie != null && movie.hasTrendingData()
                && movie.getNumVotes() >= MINIMUM_VOTES
                && movie.getRatings().signum() >= 0
                && movie.getCommentCount() >= 0
                && movie.getViewCount() >= 0;
    }

    private TrendingMovieResponse toResponse(Movie movie) {
        return new TrendingMovieResponse(movie.getMovieId(), movie.getTitle(), movie.getRatings(),
                movie.getNumVotes(), movie.getCommentCount(), movie.getViewCount(),
                scoreCalculator.calculate(movie.getRatings(), movie.getNumVotes(),
                        movie.getCommentCount(), movie.getViewCount()));
    }
}
