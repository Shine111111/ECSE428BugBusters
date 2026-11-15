package ca.mcgill.ecse428.bugbusters.dto;

import java.math.BigDecimal;

/** A snapshot of the catalog signals used to calculate a movie's trending score. */
public record TrendingMovieResponse(
        String movieId,
        String title,
        BigDecimal rating,
        long numberOfVotes,
        int commentCount,
        int viewCount,
        BigDecimal trendingScore) {
}
