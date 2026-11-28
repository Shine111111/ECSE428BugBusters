package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Person;
import ca.mcgill.ecse428.bugbusters.model.Role;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.PersonRepository;
import ca.mcgill.ecse428.bugbusters.repository.RoleRepository;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class SearchMovieStepDefinitions {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Movie> searchResults;
    private String errorMessage;

    // ID017: movies the user is currently looking at (seeded catalog, or the last sorted list)
    private List<Movie> currentMovies = new ArrayList<>();
    private List<String> sortedTitles;
    private int responseStatus;

    // Track only test IDs created during the scenario run
    private final List<String> createdMovieIds = new ArrayList<>();
    private final List<String> createdPersonIds = new ArrayList<>();

    @After
    public void cleanUpTestRecords() {
        // Only delete the specific records created during the test
        for (String mId : createdMovieIds) {
            try {
                roleRepository.findAll().stream()
                        .filter(r -> r.getMovieId().equals(mId))
                        .forEach(roleRepository::delete);
                movieRepository.deleteById(mId);
            } catch (Exception ignored) {}
        }
        for (String pId : createdPersonIds) {
            try {
                personRepository.deleteById(pId);
            } catch (Exception ignored) {}
        }
        createdMovieIds.clear();
        createdPersonIds.clear();
    }

    @Given("the following movies exist in the catalog:")
    public void the_following_movies_exist_in_the_catalog(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String title = row.get("title");
            String genre = row.get("genre");
            String directorName = row.get("director");
            String actorsList = row.get("actors");

            // ID017 tables also give rating, views and release_year; ID016 tables use the defaults
            BigDecimal rating = row.get("rating") == null ? new BigDecimal("8.0") : new BigDecimal(row.get("rating").trim());
            Long views = row.get("views") == null ? 1000L : Long.valueOf(row.get("views").trim());
            Integer releaseYear = row.get("release_year") == null ? 2020 : Integer.valueOf(row.get("release_year").trim());

            // Check if movie already exists in Supabase
            List<Movie> existing = movieRepository.findByTitleContainingIgnoreCase(title);
            Movie movie;
            if (existing.isEmpty()) {
                String newId = "test_mov_" + UUID.randomUUID().toString().substring(0, 8);
                movie = movieRepository.save(new Movie(newId, title, genre, releaseYear, 120, rating, views));
                createdMovieIds.add(newId);
            } else {
                movie = existing.get(0);
                if (row.containsKey("rating") || row.containsKey("views") || row.containsKey("release_year")) {
                    movie.setRatings(rating);
                    movie.setNumVotes(views);
                    movie.setStartYear(releaseYear);
                    movie = movieRepository.save(movie);
                }
            }
            String movieId = movie.getMovieId();
            currentMovies.add(movie);

            // Seed Director
            if (directorName != null && !directorName.isBlank()) {
                String directorId = "test_dir_" + UUID.randomUUID().toString().substring(0, 8);
                Person director = new Person(directorId, directorName.trim());
                personRepository.save(director);
                createdPersonIds.add(directorId);
                roleRepository.save(new Role(movieId, directorId, "director"));
            }

            // Seed Actors
            if (actorsList != null && !actorsList.isBlank()) {
                for (String actorName : actorsList.split(",")) {
                    String actorId = "test_act_" + UUID.randomUUID().toString().substring(0, 8);
                    Person actor = new Person(actorId, actorName.trim());
                    personRepository.save(actor);
                    createdPersonIds.add(actorId);
                    roleRepository.save(new Role(movieId, actorId, "actor"));
                }
            }
        }
    }

    @When("the user searches for movies with {word} matching {string}")
    public void the_user_searches_for_movies_with_matching(String criterion, String query) {
        try {
            searchResults = movieService.searchMovies(criterion, query);
            errorMessage = null;
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("the search results should contain the movie {string}")
    public void the_search_results_should_contain_the_movie(String expectedTitle) {
        assertNotNull(searchResults, "Search results should not be null");
        boolean exists = searchResults.stream()
                .anyMatch(m -> m.getTitle().equalsIgnoreCase(expectedTitle));
        assertTrue(exists, "Expected movie '" + expectedTitle + "' not found in search results.");
    }

    @Then("the search results should contain {int} movies")
    public void the_search_results_should_contain_movies(int expectedCount) {
        assertNotNull(searchResults);
        assertEquals(expectedCount, searchResults.size());
    }

    @Then("the results should include:")
    public void the_results_should_include(DataTable dataTable) {
        List<String> expectedTitles = dataTable.asList();
        List<String> titlesToVerify = expectedTitles.get(0).equalsIgnoreCase("title")
                ? expectedTitles.subList(1, expectedTitles.size())
                : expectedTitles;

        List<String> actualTitles = searchResults.stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());
        assertTrue(actualTitles.containsAll(titlesToVerify));
    }

    @Then("the search result list should be empty")
    public void the_search_result_list_should_be_empty() {
        assertNotNull(searchResults);
        assertTrue(searchResults.isEmpty());
    }

    @Then("an informative message {string} should be displayed")
    public void an_informative_message_should_be_displayed(String expectedMessage) {
        if (searchResults != null && searchResults.isEmpty()) {
            assertEquals("No movies found matching your criteria", expectedMessage);
        }
    }

    @When("the user submits an empty search query")
    public void the_user_submits_an_empty_search_query() {
        try {
            searchResults = movieService.searchMovies("title", "");
            errorMessage = null;
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error message {string} should be issued")
    public void an_error_message_should_be_issued(String expectedError) {
        assertEquals(expectedError, errorMessage);
    }

    // =========================================================================
    // ID017 Sort Movies
    // =========================================================================

    private void sortCurrentMovies(String criterion, String direction) {
        try {
            currentMovies = movieService.sortMovies(currentMovies, criterion, direction);
            sortedTitles = currentMovies.stream().map(Movie::getTitle).toList();
            errorMessage = null;
        } catch (IllegalArgumentException e) {
            sortedTitles = null;
            errorMessage = e.getMessage();
        }
    }

    // Accepts the criterion/direction with or without surrounding quotes
    private static String unquote(String value) {
        return value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1)
                : value;
    }

    @Given("the movie list is currently sorted by title is ascending order")
    public void the_movie_list_is_currently_sorted_by_title_ascending() {
        sortCurrentMovies("title", "ascending");
        assertNull(errorMessage);
    }

    @Given("there are no movies in the current search results")
    public void there_are_no_movies_in_the_current_search_results() {
        currentMovies = new ArrayList<>();
    }

    @When("the user sorts the movies with {word} in {word} order")
    public void the_user_sorts_the_movies_with_in_order(String criterion, String direction) {
        sortCurrentMovies(criterion, direction);
    }

    @When("the user sorts the movies by {word} in {word} order")
    public void the_user_sorts_the_movies_by_in_order(String criterion, String direction) {
        sortCurrentMovies(unquote(criterion), unquote(direction));
    }

    @When("the user attempts to sort the results by {string} in {string} order")
    public void the_user_attempts_to_sort_the_results_by_in_order(String criterion, String direction) {
        sortCurrentMovies(criterion, direction);
    }

    @When("the user calls the search endpoint for movies with {word} matching {string} sorted by {word} in {word} order")
    public void the_user_calls_the_search_endpoint_sorted(String criterion, String query, String sortBy, String direction)
            throws Exception {
        callSearchEndpoint(get("/movies/search")
                .param("criterion", criterion)
                .param("query", query)
                .param("sortBy", sortBy)
                .param("direction", direction));
    }

    @When("the user calls the search endpoint for movies with {word} matching {string} sorted by {word}")
    public void the_user_calls_the_search_endpoint_sorted_default_direction(String criterion, String query, String sortBy)
            throws Exception {
        callSearchEndpoint(get("/movies/search")
                .param("criterion", criterion)
                .param("query", query)
                .param("sortBy", sortBy));
    }

    private void callSearchEndpoint(MockHttpServletRequestBuilder request) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        responseStatus = response.getStatus();
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        if (body.isArray()) {
            List<String> titles = new ArrayList<>();
            body.forEach(movie -> titles.add(movie.get("title").asText()));
            sortedTitles = titles;
            errorMessage = null;
        } else {
            sortedTitles = null;
            errorMessage = body.path("error").asText(null);
        }
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int expectedStatus) {
        assertEquals(expectedStatus, responseStatus);
    }

    @Then("the sorted results should be:")
    public void the_sorted_results_should_be(DataTable dataTable) {
        List<String> rows = dataTable.asList();
        List<String> expectedTitles = rows.get(0).equalsIgnoreCase("title") ? rows.subList(1, rows.size()) : rows;
        assertNotNull(sortedTitles, "Expected sorted results but got error: " + errorMessage);
        assertEquals(expectedTitles, sortedTitles);
    }

    @Then("a warning message {string} should be displayed")
    public void a_warning_message_should_be_displayed(String expectedMessage) {
        assertEquals(expectedMessage, errorMessage);
    }
}