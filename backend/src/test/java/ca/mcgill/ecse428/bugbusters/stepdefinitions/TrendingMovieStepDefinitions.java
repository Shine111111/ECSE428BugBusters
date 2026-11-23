package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.service.TrendingMovieService;
import ca.mcgill.ecse428.bugbusters.service.trending.TrendingScoreCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TID047 - Acceptance-test step definitions for SID0027.
 */
public class TrendingMovieStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TrendingScoreCalculator scoreCalculator;

    private MockHttpServletResponse response;
    private JsonNode responseBody;

    private final List<String> createdMovieIds = new ArrayList<>();

    private BigDecimal previousPulpFictionScore;

    // ----------------------------------------------------------------
    // Cleanup
    // ----------------------------------------------------------------

    @After
    public void cleanUpTrendingMovies() {
        for (String movieId : createdMovieIds) {
            try {
                movieRepository.deleteById(movieId);
            } catch (Exception ignored) {
            }
        }

        createdMovieIds.clear();
        response = null;
        responseBody = null;
        previousPulpFictionScore = null;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Movie findMovie(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .filter(movie -> movie.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Movie \"" + title + "\" was not found"));
    }

    private void addMovies(DataTable table) {
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {

            String id = "trending_" +
                    UUID.randomUUID().toString().substring(0, 8);

            Movie movie = new Movie(
                    id,
                    row.get("title"),
                    "Unknown",
                    2020,
                    120,
                    new BigDecimal(row.get("rating")),
                    Long.parseLong(row.get("number_of_votes"))
            );

            movie.setCommentCount(
                    Integer.parseInt(row.get("comment_count")));

            movie.setViewCount(
                    Integer.parseInt(row.get("view_count")));

            movieRepository.save(movie);
            createdMovieIds.add(id);
        }
    }

    private List<JsonNode> moviesFromResponse() {
        assertNotNull(responseBody);

        List<JsonNode> movies = new ArrayList<>();

        responseBody.get("movies").forEach(movies::add);

        return movies;
    }

    private JsonNode responseMovie(String title) {
        return moviesFromResponse().stream()
                .filter(movie ->
                        movie.get("title").asText().equalsIgnoreCase(title))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Movie \"" + title +
                                "\" was not found in trending response"));
    }

    private List<String> responseTitles() {
        return moviesFromResponse().stream()
                .map(movie -> movie.get("title").asText())
                .toList();
    }

    // ----------------------------------------------------------------
    // Background
    // ----------------------------------------------------------------

    @Given("only movies with at least 30000 votes are eligible to appear on the trending movies page")
    public void minimumVoteRequirement() {
        assertEquals(30_000L, TrendingMovieService.MINIMUM_VOTES);
    }

    @Given("the trending score is calculated as:")
    public void trendingScoreRules(DataTable table) {
        // The actual calculation is tested through the resulting scores.
        assertFalse(table.asMaps().isEmpty());
    }

    @Given("trending movies are ranked by trending score descending")
    public void trendingMoviesAreRankedByScore() {
        // Ranking is verified by the scenarios below.
    }

    @Given("the following movies with trending data exist in the catalog:")
    public void trendingMoviesExist(DataTable table) {
        addMovies(table);
    }

    // ----------------------------------------------------------------
    // Additional Given steps
    // ----------------------------------------------------------------

    @Given("the following additional movies exist in the catalog:")
    public void additionalMoviesExist(DataTable table) {
        addMovies(table);
    }

    @Given("{string} receives {int} additional votes")
    public void movieReceivesAdditionalVotes(
            String title,
            int additionalVotes) {

        Movie movie = findMovie(title);

        previousPulpFictionScore = scoreCalculator.calculate(
                movie.getRatings(),
                movie.getNumVotes(),
                movie.getCommentCount(),
                movie.getViewCount());

        movie.setNumVotes(
                movie.getNumVotes() + additionalVotes);

        movieRepository.save(movie);
    }

    @Given("{string} receives {int} additional views")
    public void movieReceivesAdditionalViews(
            String title,
            int additionalViews) {

        Movie movie = findMovie(title);

        movie.setViewCount(
                movie.getViewCount() + additionalViews);

        movieRepository.save(movie);
    }

    @Given("there are no movies in the catalog")
    public void thereAreNoMoviesInTheCatalog() {
        movieRepository.deleteAll();
    }

    @Given("a movie exists with missing trending data")
    public void movieExistsWithMissingTrendingData() {

        String id = "trending_missing_" +
                UUID.randomUUID().toString().substring(0, 8);

        Movie movie = new Movie(
                id,
                "Missing Trending Data",
                "Drama",
                2020,
                120,
                new BigDecimal("8.0"),
                50_000L
        );

        movie.setCommentCount(100);

        // Missing view count
        movie.setViewCount(null);

        movieRepository.save(movie);
        createdMovieIds.add(id);
    }

    // ----------------------------------------------------------------
    // When
    // ----------------------------------------------------------------

    @When("the user requests to view the trending movies page")
    public void requestTrendingMovies() throws Exception {

        response = mockMvc.perform(
                        get("/movies/trending"))
                .andReturn()
                .getResponse();

        responseBody =
                objectMapper.readTree(response.getContentAsString());
    }

    // ----------------------------------------------------------------
    // Then - page/result
    // ----------------------------------------------------------------

    @Then("the trending movies page should be displayed")
    public void trendingMoviesPageDisplayed() {

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(responseBody);
        assertTrue(responseBody.has("movies"));
    }

    @Then("the trending movies should be ranked by their trending score in descending order")
    public void rankedByTrendingScore() {

        List<JsonNode> movies = moviesFromResponse();

        for (int i = 0; i < movies.size() - 1; i++) {

            BigDecimal current =
                    movies.get(i)
                            .get("trendingScore")
                            .decimalValue();

            BigDecimal next =
                    movies.get(i + 1)
                            .get("trendingScore")
                            .decimalValue();

            assertTrue(
                    current.compareTo(next) >= 0,
                    "Trending movies are not sorted by score");
        }
    }

    // ----------------------------------------------------------------
    // Then - displayed movie information
    // ----------------------------------------------------------------

    @Then("each trending movie should display its title")
    public void eachMovieDisplaysTitle() {

        moviesFromResponse().forEach(movie ->
                assertTrue(movie.hasNonNull("title")));
    }

    @Then("each trending movie should display its rating")
    public void eachMovieDisplaysRating() {

        moviesFromResponse().forEach(movie ->
                assertTrue(movie.hasNonNull("rating")));
    }

    @Then("each trending movie should display its number of votes")
    public void eachMovieDisplaysVotes() {

        moviesFromResponse().forEach(movie ->
                assertTrue(movie.hasNonNull("numberOfVotes")));
    }

    @Then("each trending movie should display its comment count")
    public void eachMovieDisplaysCommentCount() {

        moviesFromResponse().forEach(movie ->
                assertTrue(movie.hasNonNull("commentCount")));
    }

    @Then("each trending movie should display its view count")
    public void eachMovieDisplaysViewCount() {

        moviesFromResponse().forEach(movie ->
                assertTrue(movie.hasNonNull("viewCount")));
    }

    // ----------------------------------------------------------------
    // Then - eligibility
    // ----------------------------------------------------------------

    @Then("{string} should not be included in the trending movies list")
    public void movieShouldNotBeIncluded(String title) {

        assertFalse(
                responseTitles().stream()
                        .anyMatch(value ->
                                value.equalsIgnoreCase(title)));
    }

    @Then("all displayed movies should have at least 30000 votes")
    public void allMoviesMeetVoteThreshold() {

        moviesFromResponse().forEach(movie ->
                assertTrue(
                        movie.get("numberOfVotes").asLong() >= 30_000L));
    }

    // ----------------------------------------------------------------
    // Then - scores/ranking
    // ----------------------------------------------------------------

    @Then("{string} should have a higher trending score than {string}")
    public void higherTrendingScore(
            String first,
            String second) {

        BigDecimal firstScore =
                responseMovie(first)
                        .get("trendingScore")
                        .decimalValue();

        BigDecimal secondScore =
                responseMovie(second)
                        .get("trendingScore")
                        .decimalValue();

        assertTrue(firstScore.compareTo(secondScore) > 0);
    }

    @Then("{string} should be ranked above {string}")
    public void shouldBeRankedAbove(
            String first,
            String second) {

        List<String> titles = responseTitles();

        assertTrue(titles.indexOf(first) >= 0);
        assertTrue(titles.indexOf(second) >= 0);

        assertTrue(
                titles.indexOf(first) <
                titles.indexOf(second));
    }

    @Then("the trending scores include:")
    public void trendingScoresInclude(DataTable table) {

        for (Map<String, String> row :
                table.asMaps(String.class, String.class)) {

            BigDecimal expected =
                    new BigDecimal(row.get("score"));

            BigDecimal actual =
                    responseMovie(row.get("title"))
                            .get("trendingScore")
                            .decimalValue();

            assertEquals(
                    0,
                    expected.compareTo(actual),
                    "Unexpected trending score for " +
                            row.get("title"));
        }
    }

    @Then("{string} is ranked above {string} in the trending movies")
    public void rankedAboveInTrendingMovies(
            String first,
            String second) {

        List<String> titles = responseTitles();

        assertTrue(titles.contains(first));
        assertTrue(titles.contains(second));

        assertTrue(
                titles.indexOf(first) <
                titles.indexOf(second));
    }

    // ----------------------------------------------------------------
    // Then - updated popularity
    // ----------------------------------------------------------------

    @Then("the trending score for {string} should increase")
    public void trendingScoreShouldIncrease(String title) {

        BigDecimal newScore =
                responseMovie(title)
                        .get("trendingScore")
                        .decimalValue();

        assertNotNull(previousPulpFictionScore);

        assertTrue(
                newScore.compareTo(previousPulpFictionScore) > 0);
    }

    @Then("the trending movies should be reordered using the updated trending scores")
    public void reorderedUsingUpdatedScores() {
        rankedByTrendingScore();
    }

    // ----------------------------------------------------------------
    // Then - errors / empty results
    // ----------------------------------------------------------------

    @Then("the trending movies list should be empty")
    public void trendingMoviesListEmpty() {

        assertTrue(moviesFromResponse().isEmpty());
    }

    @Then("the trending movies page should display the message {string}")
    public void trendingPageDisplaysMessage(String expectedMessage) {

        assertEquals(
                expectedMessage,
                responseBody.get("message").asText());
    }

    @Then("the movie with missing trending data should not be included in the trending movies list")
    public void missingTrendingDataExcluded() {

        assertFalse(
                responseTitles().contains("Missing Trending Data"));
    }

    @Then("the trending movies page should still be displayed")
    public void trendingPageStillDisplayed() {

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(responseBody.has("movies"));
    }
}