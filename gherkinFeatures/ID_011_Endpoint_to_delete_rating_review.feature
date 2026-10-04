Feature: Endpoint to delete rating/review

  As a registered user
  I want to delete my own rating and review
  So that I can withdraw my opinion

  Background:
    Given registered users "Hamza" and "Robert" exist
    And a movie named "Cars2" exists
    And ratings must be whole numbers from 1 to 5
    And each user may have at most one rating per movie
    And a comment is optional

  Scenario: Delete a rating and review (Normal Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 4 stars with the comment "Great movie"
    When I request deletion of my rating for "Cars2"
    Then the request succeeds
    And my rating and its comment are no longer stored
    And "Cars2" and my account still exist

  Scenario: Delete a rating without a comment (Alternative Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 4 stars without a comment
    When I request deletion of my rating for "Cars2"
    Then the request succeeds
    And my rating is no longer stored

  Scenario: Reject deletion of another users rating (Error Flow)
    Given I am authenticated as "Hamza"
    And "Robert" has a rating for "Cars2"
    When I request deletion of "Robert"'s rating
    Then the request is rejected because I do not own the rating
    And "Robert"'s rating remains unchanged

  Scenario: Delete a missing rating (Alternative Flow)
    Given I am authenticated as "Hamza"
    And I have no rating for "Cars2"
    When I request deletion of my rating for "Cars2"
    Then the response indicates that no rating exists
    And no other rating is changed

  Scenario: Require authentication (Error Flow)
    Given I am not authenticated
    When I request deletion of a rating
    Then authentication is required
    And no rating is deleted
