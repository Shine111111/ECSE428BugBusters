package ca.mcgill.ecse428.bugbusters.model;

import java.util.Set;

/** Contains the user's explicit favorite genres, actors, and directors for preference scoring. */
public class UserPreferences {
    private final Set<String> favoriteGenres;
    private final Set<String> favoriteActors;
    private final Set<String> favoriteDirectors;

    public UserPreferences(Set<String> favoriteGenres, Set<String> favoriteActors,
            Set<String> favoriteDirectors) {
        this.favoriteGenres = Set.copyOf(favoriteGenres);
        this.favoriteActors = Set.copyOf(favoriteActors);
        this.favoriteDirectors = Set.copyOf(favoriteDirectors);
    }

    public Set<String> getFavoriteGenres() { return favoriteGenres; }
    public Set<String> getFavoriteActors() { return favoriteActors; }
    public Set<String> getFavoriteDirectors() { return favoriteDirectors; }
}
