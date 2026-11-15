package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.service.trending.TrendingScoreCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrendingScoreCalculatorTest {

    private final TrendingScoreCalculator calculator = new TrendingScoreCalculator();

    @Test
    void calculatesFeatureFileScoreForTheDarkKnight() {
        assertScore("1110", calculator.calculate(new BigDecimal("9.0"), 700000, 5000, 900000));
    }

    @ParameterizedTest
    @CsvSource({
            "8.5, 0, 0, 0, 85",
            "0, 10000, 0, 0, 1",
            "0, 0, 100, 0, 1",
            "0, 0, 0, 1000, 1"
    })
    void appliesEachPopularitySignalWeight(BigDecimal rating, long votes, long comments, long views,
                                           String expectedScore) {
        assertScore(expectedScore, calculator.calculate(rating, votes, comments, views));
    }

    @Test
    void preservesFractionalContributions() {
        assertScore("91.5111", calculator.calculate(new BigDecimal("8.85"), 30001, 1, 1));
    }

    @Test
    void returnsZeroWhenEveryMetricIsZero() {
        assertScore("0", calculator.calculate(BigDecimal.ZERO, 0, 0, 0));
    }

    @Test
    void commentsAndViewsIncreaseScoreWhenRatingAndVotesAreEqual() {
        BigDecimal aurora = calculator.calculate(new BigDecimal("8.5"), 400000, 2000, 300000);
        BigDecimal apollo = calculator.calculate(new BigDecimal("8.5"), 400000, 5000, 700000);

        assertScore("445", aurora);
        assertScore("875", apollo);
        assertTrue(apollo.compareTo(aurora) > 0);
    }

    @Test
    void differentSignalsCanProduceExactlyEqualScores() {
        BigDecimal aurora = calculator.calculate(new BigDecimal("8.0"), 100000, 1000, 100000);
        BigDecimal apollo = calculator.calculate(new BigDecimal("8.0"), 90000, 1100, 100000);

        assertScore("200", aurora);
        assertEquals(0, aurora.compareTo(apollo));
    }

    @Test
    void supportsCountsBeyondIntegerRangeWithoutOverflow() {
        assertScore("102379429609088011.4577",
                calculator.calculate(BigDecimal.ZERO, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
    }

    @Test
    void rejectsMissingRating() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null, 30000, 0, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "-0.1, 30000, 0, 0",
            "8.0, -1, 0, 0",
            "8.0, 30000, -1, 0",
            "8.0, 30000, 0, -1"
    })
    void rejectsEachNegativeMetric(BigDecimal rating, long votes, long comments, long views) {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(rating, votes, comments, views));
    }

    private void assertScore(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "Unexpected score: " + actual);
    }
}
