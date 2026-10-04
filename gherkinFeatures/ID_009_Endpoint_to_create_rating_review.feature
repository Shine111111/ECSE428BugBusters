Feature: Endpoint to create rating/review

  As a registered user
  I want to submit a movie rating with an optional review
  So that I can share my opinion

  Background:
    Given registered users "Hamza" and "Robert" exist
    And a movie named "Cars2" exists
    And ratings must be whole numbers from 1 to 5
    And each user may have at most one rating per movie
    And a comment is optional

  Scenario: Create a rating with a review (Normal Flow)
    Given I am authenticated as "Hamza"
    And I have not rated "Cars2"
    When I request creation of a 4-star rating for "Cars2" with the comment "Great movie"
    Then the request succeeds
    And my saved rating is 4 stars with the comment "Great movie"

  Scenario: Create a rating without a review (Alternative Flow)
    Given I am authenticated as "Hamza"
    And I have not rated "Cars2"
    When I request creation of a 5-star rating for "Cars2" without a comment
    Then the request succeeds
    And my saved rating is 5 stars without a comment

  Scenario Outline: Reject invalid ratings (Error Flow)
    Given I am authenticated as "Hamza"
    And I have not rated "Cars2"
    When I request creation of a rating with value "<rating>" for "Cars2"
    Then the request is rejected as an invalid rating
    And no rating is created

    Examples:
    | rating |
    | 0 |
    | 6 |
    | -1 |
    | 2.5 |
    | abc |
    | |

  Scenario: Reject an unauthenticated request (Error Flow)
    Given I am not authenticated
    When I request creation of a 4-star rating for "Cars2"
    Then authentication is required
    And no rating is created

  Scenario: Reject a duplicate rating (Error Flow)
    Given I am authenticated as "Hamza"
    And I already have a 3-star rating for "Cars2"
    When I request creation of another rating for "Cars2"
    Then the request is rejected because my rating already exists
    And my existing 3-star rating remains unchanged

  Scenario: Reject a nonexistent movie (Error Flow)
    Given I am authenticated as "Hamza"
    And movie "UNKNOWN_MOVIE" does not exist
    When I request creation of a 4-star rating for "UNKNOWN_MOVIE"
    Then the request is rejected because the movie does not exist
    And no rating is created
