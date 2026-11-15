package ca.mcgill.ecse428.bugbusters.service.trending;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Calculates the SID0027 weighted popularity score without rounding its signals. */
@Component
public class TrendingScoreCalculator {

    /**
     * Score = rating * 10 + votes / 10000 + comments / 100 + views / 1000.
     * Missing or negative data must be excluded before a movie is scored.
     */
    public BigDecimal calculate(BigDecimal rating, long numberOfVotes, long commentCount, long viewCount) {
        if (rating == null || rating.signum() < 0
                || numberOfVotes < 0 || commentCount < 0 || viewCount < 0) {
            throw new IllegalArgumentException("Trending metrics must be present and non-negative");
        }

        return rating.multiply(BigDecimal.TEN)
                .add(BigDecimal.valueOf(numberOfVotes).movePointLeft(4))
                .add(BigDecimal.valueOf(commentCount).movePointLeft(2))
                .add(BigDecimal.valueOf(viewCount).movePointLeft(3));
    }
}
