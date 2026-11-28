package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.service.MovieService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class SortMovieStepDefinitions {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieScenarioContext scenarioContext;

    private List<Movie> currentMovies;
    private List<Movie> sortedResults;
    private String warningMessage;

    @Before
    public void resetSortState() {
        currentMovies = null;
        sortedResults = null;
        warningMessage = null;
    }

    private List<Movie> activeList() {
        return currentMovies != null ? currentMovies : scenarioContext.getCatalogMovies();
    }

    private void executeSort(String criterion, String direction) {
        try {
            sortedResults = movieService.sortMovies(activeList(), criterion, direction);
            currentMovies = sortedResults;
            scenarioContext.setErrorMessage(null);
            warningMessage = null;
        } catch (IllegalArgumentException e) {
            sortedResults = null;
            scenarioContext.setErrorMessage(e.getMessage());
            warningMessage = e.getMessage();
        }
    }

    @When("the user sorts the movies with {word} in {word} order")
    public void the_user_sorts_the_movies_with_in_order(String criterion, String direction) {
        executeSort(criterion, direction);
    }
    

    @Given("the movie list is currently sorted by title is ascending order")
    public void the_movie_list_is_currently_sorted_by_title_is_ascending_order() {
        currentMovies = movieService.sortMovies(scenarioContext.getCatalogMovies(), "title", "ascending");
    }

    // @When("the user sorts the movies by {word} in {word} order")
    // public void the_user_sorts_the_movies_by_word_in_word_order(String criterion, String direction) {
    //     executeSort(criterion, direction);
    // }

    @When("^the user sorts the movies by ([a-zA-Z_]+) in ([a-zA-Z_]+) order$")
    public void the_user_sorts_the_movies_by_word_in_word_order(String criterion, String direction) {
        executeSort(criterion, direction);
    }

    @When("the user sorts the movies by {string} in {string} order")
    public void the_user_sorts_the_movies_by_string_in_string_order(String criterion, String direction) {
        executeSort(criterion, direction);
    }
    

    @Given("there are no movies in the current search results")
    public void there_are_no_movies_in_the_current_search_results() {
        currentMovies = new ArrayList<>();
    }

    @When("the user attempts to sort the results by {string} in {string} order")
    public void the_user_attempts_to_sort_the_results_by_in_order(String criterion, String direction) {
        executeSort(criterion, direction);
    }

    @Then("the sorted results should be:")
    public void the_sorted_results_should_be(DataTable dataTable) {
        assertNotNull(sortedResults, "Expected sorted results, but got error: " + scenarioContext.getErrorMessage());

        List<String> expectedTitles = dataTable.asMaps(String.class, String.class)
                .stream()
                .map(row -> row.get("title"))
                .toList();

        List<String> actualTitles = sortedResults.stream()
                .map(Movie::getTitle)
                .toList();

        assertEquals(expectedTitles, actualTitles);
    }

    @Then("a warning message {string} should be displayed")
    public void a_warning_message_should_be_displayed(String expectedWarning) {
        assertEquals(expectedWarning, warningMessage);
    }
}