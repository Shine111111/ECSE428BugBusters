Feature: Endpoint to update rating/review

  As a registered user
  I want to change my own rating and review
  So that they reflect my current opinion

  Background:
    Given registered users "Hamza" and "Robert" exist
    And a movie named "Cars2" exists
    And ratings must be whole numbers from 1 to 5
    And each user may have at most one rating per movie
    And a comment is optional

  Scenario: Update a rating and review (Normal Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 3 stars with the comment "Good"
    When I request an update to 5 stars with the comment "Excellent"
    Then the request succeeds
    And my rating is 5 stars with the comment "Excellent"
    And I still have exactly one rating for "Cars2"

  Scenario: Remove an optional comment (Alternative Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 4 stars with the comment "Great movie"
    When I request an update to 4 stars without a comment
    Then the request succeeds
    And my rating is 4 stars without a comment

  Scenario: Reject an invalid update (Error Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 3 stars
    When I request an update to 6 stars
    Then the request is rejected as an invalid rating
    And my rating remains 3 stars

  Scenario: Reject changing another users rating (Error Flow)
    Given I am authenticated as "Hamza"
    And "Robert" has a 2-star rating for "Cars2"
    When I request an update to "Robert"'s rating
    Then the request is rejected because I do not own the rating
    And "Robert"'s rating remains unchanged

  Scenario: Reject updating a missing rating (Error Flow)
    Given I am authenticated as "Hamza"
    And I have no rating for "Cars2"
    When I request an update to my rating for "Cars2"
    Then the request is rejected because the rating does not exist
    And no rating is created

  Scenario: Require authentication (Error Flow)
    Given I am not authenticated
    When I request an update to a rating
    Then authentication is required
    And no rating is changed
