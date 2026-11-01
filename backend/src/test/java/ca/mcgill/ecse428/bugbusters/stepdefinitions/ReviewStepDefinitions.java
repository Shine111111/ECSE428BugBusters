package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import ca.mcgill.ecse428.bugbusters.model.Movie;
import ca.mcgill.ecse428.bugbusters.model.Review;
import ca.mcgill.ecse428.bugbusters.repository.MovieRepository;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.ReviewService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewStepDefinitions {

    @Autowired
    private MovieRepository movieRepository;

    private ReviewRepository reviewRepository;
    private ReviewService reviewService;

    private final Set<String> registeredUsers = new HashSet<>();
    private String loggedInUser;

    // Movie titles used in the feature file mapped to the IDs seeded in the database
    private final Map<String, String> movieIdsByTitle = new HashMap<>();
    private final List<String> createdMovieIds = new ArrayList<>();

    private String errorMessage;
    private Review lastSubmittedReview;
    // Steps worded the same way are used both to set up (Given) and to check (Then)
    private boolean submissionMade;

    @Before
    public void setUp() {
        reviewRepository = new ReviewRepository();
        reviewService = new ReviewService(reviewRepository, movieRepository);
    }

    @After
    public void cleanUpTestRecords() {
        // Only delete the movies created during the test
        for (String movieId : createdMovieIds) {
            try {
                movieRepository.deleteById(movieId);
            } catch (Exception ignored) {}
        }
        createdMovieIds.clear();
    }

    private String movieId(String title) {
        return movieIdsByTitle.getOrDefault(title, title);
    }

    private void submit(String userId, String movieId, int stars, String reviewText) {
        submissionMade = true;
        lastSubmittedReview = new Review(userId, movieId, stars, reviewText);
        try {
            reviewService.storeReview(lastSubmittedReview, loggedInUser);
            errorMessage = null;
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    private Review reviewOf(String user, String title) {
        Review review = reviewRepository.findByUserAndMovie(user, movieId(title));
        assertNotNull(review, "Expected " + user + " to have a review of " + title);
        return review;
    }

    @Given("a registered user {string} exists")
    public void a_registered_user_exists(String user) {
        registeredUsers.add(user);
    }

    @Given("{string} is logged in")
    public void is_logged_in(String user) {
        assertTrue(registeredUsers.contains(user), user + " is not a registered user");
        loggedInUser = user;
    }

    @Given("{string} has logged out")
    public void has_logged_out(String user) {
        assertEquals(user, loggedInUser);
        loggedInUser = null;
    }

    @Given("a movie named {string} exists")
    public void a_movie_named_exists(String title) {
        List<Movie> existing = movieRepository.findByTitleContainingIgnoreCase(title);
        String movieId = existing.stream()
                .filter(m -> m.getTitle().equalsIgnoreCase(title))
                .map(Movie::getMovieId)
                .findFirst()
                .orElse(null);
        if (movieId == null) {
            movieId = "test_mov_" + UUID.randomUUID().toString().substring(0, 8);
            movieRepository.save(new Movie(movieId, title, "Animation", 2011, 106, new BigDecimal("6.2"), 1000L));
            createdMovieIds.add(movieId);
        }
        movieIdsByTitle.put(title, movieId);
    }

    @Given("no movie with identifier {string} exists")
    public void no_movie_with_identifier_exists(String movieId) {
        assertFalse(movieRepository.existsById(movieId));
    }

    @Given("{string} has not rated {string}")
    public void has_not_rated(String user, String title) {
        assertNull(reviewRepository.findByUserAndMovie(user, movieId(title)));
    }

    @Given("{string} has rated {string} with {int} stars")
    public void has_rated_with_stars(String user, String title, int stars) {
        reviewService.storeReview(new Review(user, movieId(title), stars, null));
    }

    @When("{string} submits a rating of {int} stars for {string} without review text")
    public void submits_a_rating_without_review_text(String user, int stars, String title) {
        submit(user, movieId(title), stars, null);
    }

    @When("{string} submits a rating of {int} stars for {string} with the review text {string}")
    public void submits_a_rating_with_review_text(String user, int stars, String title, String text) {
        submit(user, movieId(title), stars, text);
    }

    @When("{string} submits a new rating of {int} stars for {string} with the review text {string}")
    public void submits_a_new_rating_with_review_text(String user, int stars, String title, String text) {
        submit(user, movieId(title), stars, text);
    }

    @When("{string} submits a rating of {int} stars for movie {string}")
    public void submits_a_rating_for_movie_id(String user, int stars, String movieId) {
        submit(user, movieId, stars, null);
    }

    @When("{string} attempts to submit a rating of {int} stars for {string}")
    public void attempts_to_submit_a_rating(String user, int stars, String title) {
        submit(user, movieId(title), stars, null);
    }

    @When("{string} submits the rating value {string} for {string}")
    public void submits_the_rating_value(String user, String rawRating, String title) {
        submissionMade = true;
        try {
            int stars = reviewService.parseRating(rawRating);
            submit(user, movieId(title), stars, null);
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("the rating is created successfully")
    public void the_rating_is_created_successfully() {
        assertNull(errorMessage);
        assertNotNull(reviewRepository.findByUserAndMovie(
                lastSubmittedReview.getUserId(), lastSubmittedReview.getMovieId()));
    }

    @Then("the rating and review are created successfully")
    public void the_rating_and_review_are_created_successfully() {
        the_rating_is_created_successfully();
    }

    @Then("{string}'s rating is created successfully")
    public void users_rating_is_created_successfully(String user) {
        assertEquals(user, lastSubmittedReview.getUserId());
        the_rating_is_created_successfully();
    }

    @Then("{string}'s rating of {string} has {int} stars")
    public void users_rating_has_stars(String user, String title, int stars) {
        assertEquals(stars, reviewOf(user, title).getRating());
    }

    @Then("{string}'s rating of {string} remains {int} stars")
    public void users_rating_remains_stars(String user, String title, int stars) {
        users_rating_has_stars(user, title, stars);
    }

    @Then("the rating has no review text")
    public void the_rating_has_no_review_text() {
        Review review = reviewRepository.findByUserAndMovie(
                lastSubmittedReview.getUserId(), lastSubmittedReview.getMovieId());
        assertNull(review.getReviewText());
    }

    @Then("{string}'s review of {string} has the text {string}")
    public void users_review_has_the_text(String user, String title, String text) {
        Review review = reviewOf(user, title);
        if (!submissionMade) {
            // Used as a Given: add the review text to the existing rating
            reviewService.updateReview(user, movieId(title), review.getRating(), text);
            return;
        }
        assertEquals(text, review.getReviewText());
    }

    @Then("{string}'s review of {string} still has the text {string}")
    public void users_review_still_has_the_text(String user, String title, String text) {
        assertEquals(text, reviewOf(user, title).getReviewText());
    }

    @Then("the rating is rejected")
    public void the_rating_is_rejected() {
        assertNotNull(errorMessage, "Expected the rating to be rejected");
    }

    @Then("the new rating and review are rejected")
    public void the_new_rating_and_review_are_rejected() {
        the_rating_is_rejected();
    }

    @Then("the rating is rejected because the movie does not exist")
    public void the_rating_is_rejected_because_the_movie_does_not_exist() {
        assertEquals("Movie does not exist", errorMessage);
    }

    @Then("{string} is informed that the rating must be a whole number from 1 to 5")
    public void is_informed_rating_must_be_1_to_5(String user) {
        assertEquals("Rating must be between 1 and 5", errorMessage);
    }

    @Then("{string} is prompted to log in")
    public void is_prompted_to_log_in(String user) {
        assertEquals("User must be logged in to rate a movie", errorMessage);
    }

    @Then("{string} is informed that he has already rated this movie")
    public void is_informed_already_rated(String user) {
        assertEquals("User has already rated this movie", errorMessage);
    }

    @Then("no rating or review by {string} is created for {string}")
    public void no_rating_is_created_for(String user, String title) {
        assertNull(reviewRepository.findByUserAndMovie(user, movieId(title)));
    }

    @Then("no rating or review by {string} is created for movie {string}")
    public void no_rating_is_created_for_movie_id(String user, String movieId) {
        assertNull(reviewRepository.findByUserAndMovie(user, movieId));
    }

    @Then("only one rating by {string} exists for {string}")
    public void only_one_rating_exists(String user, String title) {
        long count = reviewService.getReviewsForUser(user).stream()
                .filter(r -> r.getMovieId().equals(movieId(title)))
                .count();
        assertEquals(1, count);
    }
}
