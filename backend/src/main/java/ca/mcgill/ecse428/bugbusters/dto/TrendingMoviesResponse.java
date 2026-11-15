package ca.mcgill.ecse428.bugbusters.dto;

import java.util.List;

/** The page data and an optional message when no trending movies are available. */
public record TrendingMoviesResponse(List<TrendingMovieResponse> movies, String message) {
    public TrendingMoviesResponse {
        movies = List.copyOf(movies);
    }
}
