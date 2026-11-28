package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MovieScenarioContext {

    private final List<Movie> catalogMovies = new ArrayList<>();
    private String errorMessage;

    public void reset() {
        catalogMovies.clear();
        errorMessage = null;
    }

    public List<Movie> getCatalogMovies() {
        return catalogMovies;
    }

    public void addCatalogMovie(Movie movie) {
        catalogMovies.add(movie);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}