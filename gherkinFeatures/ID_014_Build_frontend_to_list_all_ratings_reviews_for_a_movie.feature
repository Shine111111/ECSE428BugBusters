Feature: Build frontend to list all ratings/reviews for a movie

  As a movie app user
  I want to view ratings and reviews for a movie
  So that I can consider other users opinions

  Background:
    Given a movie named "Cars2" exists

  Scenario: Display ratings and reviews (Normal Flow)
    Given the following ratings exist for "Cars2":
    | author | stars | comment |
    | Hamza | 4 | Great movie |
    | Robert | 2 | Too long |
    When I request the ratings and reviews for "Cars2"
    Then the list includes "Hamza" with 4 stars and "Great movie"
    And the list includes "Robert" with 2 stars and "Too long"

  Scenario: Display a rating without a comment (Alternative Flow)
    Given "Hamza" has rated "Cars2" with 5 stars without a comment
    When I request the ratings and reviews for "Cars2"
    Then the list includes "Hamza" with 5 stars
    And no comment is displayed for that rating

  Scenario: No ratings yet (Alternative Flow)
    Given "Cars2" has no ratings
    When I request the ratings and reviews for "Cars2"
    Then I am informed that no ratings or reviews are available
    And no invented ratings are displayed

  Scenario: Exclude ratings for other movies (Normal Flow)
    Given "Hamza" has rated "Cars2" with 4 stars
    And "Robert" has rated "Titanic" with 2 stars
    When I request the ratings and reviews for "Cars2"
    Then "Hamza"'s rating is included
    And "Robert"'s rating for "Titanic" is excluded

  Scenario: Handle a failed retrieval (Error Flow)
    Given the rating service is unavailable
    When I request the ratings and reviews for "Cars2"
    Then I am informed that ratings and reviews could not be loaded
    And the failure is not presented as a movie with no ratings
