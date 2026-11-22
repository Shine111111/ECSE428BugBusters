package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.model.WatchLaterEntry;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.repository.WatchLaterRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

// Step definitions for ID_023_Add_Movie_To_Watch_Later.feature.
// Movies are added and listed through the REST endpoints, the same way the frontend will call them:
//   POST /users/{userId}/watch-later/{movieId}  and  GET /users/{userId}/watch-later
// The catalog movies in the Background are seeded (and removed after each scenario) by the shared
// "the following movies exist in the catalog:" step in SearchMovieStepDefinitions.
public class WatchLaterStepDefinitions {

    private static final String USER_ID = "9023";
    private static final String USER_EMAIL = "watch.later.tester@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private WatchLaterRepository watchLaterRepository;

    private String userId;
    private MockHttpServletResponse response;
    private JsonNode responseBody;
    private List<String> playlistBeforeAdd;

    @After
    public void removeWatchLaterEntries() {
        if (userId != null) {
            watchLaterRepository.deleteAll(watchLaterRepository.findByUserIdOrderByAddedAtDescIdDesc(userId));
        }
    }

    // ---- helpers ----

    private String movieIdForTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .filter(movie -> movie.getTitle().equalsIgnoreCase(title))
                .map(Movie::getMovieId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No movie titled \"" + title + "\" in the catalog"));
    }

    private List<String> playlistMovieIds() throws Exception {
        MockHttpServletResponse listResponse = mockMvc.perform(get("/users/{userId}/watch-later", userId))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), listResponse.getStatus(),
                "Listing the playlist failed: " + listResponse.getContentAsString());
        return objectMapper.readValue(listResponse.getContentAsString(), new TypeReference<List<String>>() { });
    }

    // ---- Background ----

    @Given("I am logged in as a user")
    public void iAmLoggedInAsAUser() {
        if (!userRepository.existsById(USER_ID)) {
            userRepository.save(new User(USER_ID, "Watch Later Tester", USER_EMAIL, "Quack123#"));
        }
        userId = USER_ID;
        // every scenario starts from the user's playlist being empty; Given steps add what they need
        removeWatchLaterEntries();
    }

    // ---- Given ----

    @Given("my Watch Later playlist contains the movie with title {string}")
    public void myPlaylistContainsTheMovie(String title) {
        String movieId = movieIdForTitle(title);
        if (!watchLaterRepository.existsByUserIdAndMovieId(userId, movieId)) {
            watchLaterRepository.save(new WatchLaterEntry(userId, movieId));
        }
        assertTrue(watchLaterRepository.existsByUserIdAndMovieId(userId, movieId));
    }

    @Given("my Watch Later playlist does not contain the movie with title {string}")
    public void myPlaylistDoesNotContainTheMovie(String title) {
        assertFalse(watchLaterRepository.existsByUserIdAndMovieId(userId, movieIdForTitle(title)));
    }

    @Given("my Watch Later playlist is empty")
    public void myPlaylistIsEmpty() {
        assertEquals(0, watchLaterRepository.countByUserId(userId));
    }

    // ---- When ----

    @When("I add the movie with title {string} to my Watch Later playlist")
    public void iAddTheMovieToMyPlaylist(String title) throws Exception {
        playlistBeforeAdd = playlistMovieIds();
        response = mockMvc.perform(post("/users/{userId}/watch-later/{movieId}", userId, movieIdForTitle(title)))
                .andReturn().getResponse();
        responseBody = objectMapper.readTree(response.getContentAsString());
    }

    // ---- Then ----

    @Then("the movie with title {string} should appear in my Watch Later playlist")
    public void theMovieShouldAppearInMyPlaylist(String title) throws Exception {
        assertEquals(HttpStatus.CREATED.value(), response.getStatus(), "Adding the movie failed: " + responseBody);
        List<String> playlist = playlistMovieIds();
        String movieId = movieIdForTitle(title);
        assertTrue(playlist.contains(movieId), "Playlist " + playlist + " does not contain " + movieId);
        assertEquals(playlistBeforeAdd.size() + 1, playlist.size());
    }

    @Then("I should see the message {string}")
    public void iShouldSeeTheMessage(String message) {
        assertEquals(HttpStatus.CREATED.value(), response.getStatus(), "Adding the movie failed: " + responseBody);
        assertEquals(message, responseBody.get("message").asText());
    }

    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String message) {
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus(), "Expected the add to be rejected: " + responseBody);
        assertEquals(message, responseBody.get("error").asText());
    }

    @Then("my Watch Later playlist should remain unchanged")
    public void myPlaylistShouldRemainUnchanged() throws Exception {
        assertEquals(playlistBeforeAdd, playlistMovieIds());
    }
}
