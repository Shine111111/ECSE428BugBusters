Feature: Endpoint to retrieve a user's rating/review

  As a registered user
  I want to retrieve my rating and review for a movie
  So that I can see my previous opinion

  Background:
    Given registered users "Hamza" and "Robert" exist
    And a movie named "Cars2" exists
    And ratings must be whole numbers from 1 to 5
    And each user may have at most one rating per movie
    And a comment is optional

  Scenario: Retrieve a rating with a review (Normal Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 4 stars with the comment "Great movie"
    When I request my rating for "Cars2"
    Then the response contains my 4-star rating and the comment "Great movie"
    And the response identifies "Hamza" as the author and "Cars2" as the movie

  Scenario: Retrieve a rating without a review (Alternative Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 5 stars without a comment
    When I request my rating for "Cars2"
    Then the response contains my 5-star rating without a comment

  Scenario: No existing rating (Alternative Flow)
    Given I am authenticated as "Hamza"
    And I have no rating for "Cars2"
    When I request my rating for "Cars2"
    Then the response indicates that I have not rated the movie
    And no rating is created

  Scenario: Keep the requested users rating distinct (Normal Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 4 stars
    And "Robert"'s rating for "Cars2" is 2 stars
    When I request my rating for "Cars2"
    Then the response contains my 4-star rating
    And it does not substitute "Robert"'s rating for mine

  Scenario: Require authentication for retrieving my rating (Error Flow)
    Given I am not authenticated
    When I request my rating for "Cars2"
    Then authentication is required

  Scenario: Reject a nonexistent movie (Error Flow)
    Given I am authenticated as "Hamza"
    And movie "UNKNOWN_MOVIE" does not exist
    When I request my rating for "UNKNOWN_MOVIE"
    Then the response indicates that the movie does not exist
