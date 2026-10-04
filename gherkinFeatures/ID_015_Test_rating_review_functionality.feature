Feature: Test rating/review functionality

  As a team member
  I want acceptance tests for the complete rating and review lifecycle
  So that I can verify the feature behaves consistently

  Background:
    Given registered users "Hamza" and "Robert" exist
    And a movie named "Cars2" exists
    And ratings must be whole numbers from 1 to 5
    And each user may have at most one rating per movie
    And a comment is optional

  Scenario: Complete the rating lifecycle (Normal Flow)
    Given I am authenticated as "Hamza"
    And I have not rated "Cars2"
    When I create a 4-star rating with the comment "Great movie"
    And I retrieve my rating
    Then the retrieved rating is 4 stars with the comment "Great movie"
    When I update my rating to 5 stars with the comment "Excellent"
    And I retrieve my rating again
    Then the retrieved rating is 5 stars with the comment "Excellent"
    When I delete my rating
    And I retrieve my rating again
    Then the response indicates that I have not rated the movie
    And my review is absent from the movie reviews

  Scenario: Complete the lifecycle without a comment (Alternative Flow)
    Given I am authenticated as "Hamza"
    And I have not rated "Cars2"
    When I create a 3-star rating without a comment
    And I retrieve my rating
    Then the retrieved rating is 3 stars without a comment

  Scenario: An invalid update preserves saved data (Error Flow)
    Given I am authenticated as "Hamza"
    And my rating for "Cars2" is 4 stars with the comment "Great movie"
    When I attempt to update the rating to 6 stars
    Then the update is rejected
    And retrieving my rating returns 4 stars with the comment "Great movie"

  Scenario Outline: Protect another users rating (Error Flow)
    Given I am authenticated as "Robert"
    And "Hamza" has a 4-star rating for "Cars2"
    When I attempt to <operation> "Hamza"'s rating
    Then the operation is rejected because I do not own the rating
    And "Hamza"'s rating remains 4 stars

    Examples:
    | operation |
    | update |
    | delete |

  Scenario: Reject an unauthenticated creation (Error Flow)
    Given I am not authenticated
    When I attempt to create a 4-star rating for "Cars2"
    Then authentication is required
    And no rating is created
