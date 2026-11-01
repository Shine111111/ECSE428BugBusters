package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.model.UserPreferences;
import ca.mcgill.ecse428.bugbusters.service.recommendation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Coordinates data retrieval, watched-movie filtering, scoring, and ranking.
 * Loads all data, filters candidates before scoring, sums the three scores, and returns the ranked recommendations.
 */
public class RecommendationService {
    private final RecommendationDataProvider dataProvider;
    private final WatchedMovieFilter watchedMovieFilter;
    private final PreferenceScorer preferenceScorer;
    private final RatingHistoryScorer ratingHistoryScorer;
    private final PopularityScorer popularityScorer;
    private final RecommendationRanker recommendationRanker;

    public RecommendationService(                               // constructor
            RecommendationDataProvider dataProvider,
            WatchedMovieFilter watchedMovieFilter,
            PreferenceScorer preferenceScorer,
            RatingHistoryScorer ratingHistoryScorer,
            PopularityScorer popularityScorer,
            RecommendationRanker recommendationRanker) {
        this.dataProvider = dataProvider;
        this.watchedMovieFilter = watchedMovieFilter;
        this.preferenceScorer = preferenceScorer;
        this.ratingHistoryScorer = ratingHistoryScorer;
        this.popularityScorer = popularityScorer;
        this.recommendationRanker = recommendationRanker;
    }

    public List<MovieRecommendationResponse> getRecommendations(String userId) {
        
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("User ID cannot be blank");

        // retrieve necessary data
        List<Movie> movies = dataProvider.getMovies();
        UserPreferences preferences = dataProvider.getPreferences(userId);
        List<Review> ratings = dataProvider.getRatings(userId);
        Set<String> watchedMovieIds = new java.util.HashSet<>(dataProvider.getWatchedMovieIds(userId));
        ratings.forEach(review -> watchedMovieIds.add(review.getMovieId()));
        
        Map<String, Movie> moviesById = movies.stream().collect(Collectors.toMap(Movie::getId,Function.identity())); // all movies metadata

        List<Movie> candidates = watchedMovieFilter.filterUnwatched(movies, watchedMovieIds);                        // filtering of already watched movies

        // calculated score for eligible movies
        List<MovieRecommendationResponse> recommendations = candidates.stream()
                        .map(movie -> {
                            double preferenceScore = preferenceScorer.score(movie, preferences);
                            double ratingHistoryScore = ratingHistoryScorer.score(movie,ratings,moviesById);
                            double popularityScore = popularityScorer.score(movie);
                            double totalScore = preferenceScore + ratingHistoryScore + popularityScore;
                            return new MovieRecommendationResponse(movie, totalScore);
                        }).toList();
        
        return recommendationRanker.rank(recommendations);      // recommendation sorting/ordering
    }
}
