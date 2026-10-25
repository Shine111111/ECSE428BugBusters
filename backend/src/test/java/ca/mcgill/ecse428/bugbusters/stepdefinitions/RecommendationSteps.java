package ca.mcgill.ecse428.bugbusters.stepdefinitions;
import ca.mcgill.ecse428.bugbusters.model.*;
import ca.mcgill.ecse428.bugbusters.dto.*;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.*;
import ca.mcgill.ecse428.bugbusters.service.recommendation.*;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class RecommendationSteps {
    private final ReviewService reviews = new ReviewService(new ReviewRepository());
    private final InMemoryRecommendationDataProvider data = new InMemoryRecommendationDataProvider(reviews);
    private final Set<String> genres = new HashSet<>(), actors = new HashSet<>(), directors = new HashSet<>(), watched = new HashSet<>();
    private List<MovieRecommendationResponse> result;
    private String user;
    @Given("the app is open") public void open() { }
    @Given("the user is logged in as {string}") public void login(String id) { user = id; }
    @Given("only unwatched movies are eligible for recommendation") public void unwatched() { }
    @Given("the recommendation scoring rules are:") public void rules(DataTable table) { }
    @Given("the popularity bonus is calculated as:") public void popularity(DataTable table) { }
    @Given("recommendations are sorted by total score descending, average rating descending, view count descending, and title ascending") public void sorting() { }
    @Given("the app has the following movies available:") public void movies(DataTable table) { addMovies(table); }
    @Given("the app also has the following unwatched movies available:") public void moreMovies(DataTable table) { addMovies(table); }
    private Set<String> split(String value) { return Set.of(value.split(", *")); }
    private void addMovies(DataTable table) {
        for (Map<String,String> row : table.asMaps()) data.saveMovie(new Movie(row.get("title"), row.get("title"),
            split(row.get("genres")), split(row.get("actors")), row.get("director"), Double.parseDouble(row.get("average rating")),
            Integer.parseInt(row.get("rating count")), Integer.parseInt(row.get("comment count")), Integer.parseInt(row.get("view count"))));
    }
    @Given("the user's favorite genres include {string}") public void genre(String value) { genres.add(value); }
    @Given("the user's favorite actors include {string}") public void actor(String value) { actors.add(value); }
    @Given("the user's favorite directors include {string}") public void director(String value) { directors.add(value); }
    @Given("the user has no favorite genres") public void noGenres() { genres.clear(); }
    @Given("the user has no favorite actors") public void noActors() { actors.clear(); }
    @Given("the user has no favorite directors") public void noDirectors() { directors.clear(); }
    @Given("the user has no watched movies") public void noWatched() { watched.clear(); }
    @Given("the user has no movie ratings") public void noRatings() { assertTrue(reviews.getReviewsForUser(user).isEmpty()); }
    @Given("the user has watched {string}") public void watched(String title) { watched.add(title); }
    @Given("the user has watched and rated {string} {int} stars") public void rated(String title, int rating) {
        watched.add(title); reviews.storeReview(new Review(user, title, rating, null));
    }
    @When("the user requests personalized movie recommendations") public void request() {
        data.savePreferences(user, new UserPreferences(genres, actors, directors));
        data.saveWatchedMovies(user, watched);
        result = new RecommendationService(data, new WatchedMovieFilter(), new PreferenceScorer(), new RatingHistoryScorer(),
            new PopularityScorer(), new RecommendationRanker()).getRecommendations(user);
    }
    @Then("the recommendation list is displayed") public void displayed() { assertNotNull(result); }
    @Then("the recommendation scores include:") public void scores(DataTable table) {
        for (Map<String,String> row : table.asMaps()) assertEquals(Double.parseDouble(row.get("score")),
            result.stream().filter(r -> r.getMovie().getTitle().equals(row.get("title"))).findFirst().orElseThrow().getTotalScore(), 0.00001);
    }
    private List<String> titles() { return result.stream().map(r -> r.getMovie().getTitle()).toList(); }
    @Then("{string} is ranked above {string}") public void above(String first, String second) {
        assertTrue(titles().containsAll(List.of(first, second))); assertTrue(titles().indexOf(first) < titles().indexOf(second));
    }
    @Then("the recommendations do not include {string}") public void excluded(String title) { assertFalse(titles().contains(title)); }
    @Then("{string} is the first recommendation") public void first(String title) { assertEquals(title, titles().get(0)); }
}
