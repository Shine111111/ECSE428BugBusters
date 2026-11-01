package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Person;
import ca.mcgill.ecse428.bugbusters.model.Role;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.PersonRepository;
import ca.mcgill.ecse428.bugbusters.repository.RoleRepository;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SearchMovieStepDefinitions {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    private List<Movie> searchResults;
    private String errorMessage;

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

            // Check if movie already exists in Supabase
            List<Movie> existing = movieRepository.findByTitleContainingIgnoreCase(title);
            String movieId;
            if (existing.isEmpty()) {
                movieId = "test_mov_" + UUID.randomUUID().toString().substring(0, 8);
                Movie movie = new Movie(movieId, title, genre, 2020, 120, new BigDecimal("8.0"), 1000L);
                movieRepository.save(movie);
                createdMovieIds.add(movieId);
            } else {
                movieId = existing.get(0).getMovieId();
            }

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
}