package ca.mcgill.ecse428.bugbusters.service.recommendation;
import ca.mcgill.ecse428.bugbusters.model.*;
public class PreferenceScorer {
    public double score(Movie movie, UserPreferences preferences) {
        return movie.getGenres().stream().filter(preferences.getFavoriteGenres()::contains).count() * 30
            + movie.getActors().stream().filter(preferences.getFavoriteActors()::contains).count() * 25
            + (preferences.getFavoriteDirectors().contains(movie.getDirector()) ? 25 : 0);
    }
}
