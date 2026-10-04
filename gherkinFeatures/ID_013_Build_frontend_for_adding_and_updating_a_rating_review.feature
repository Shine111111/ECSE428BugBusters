Feature: Build frontend for adding and updating a rating/review

  As a registered user
  I want to select stars and optionally write or edit a review
  So that I can express my opinion easily

  Background:
    Given registered users "Hamza" and "Robert" exist
    And a movie named "Cars2" exists
    And ratings must be whole numbers from 1 to 5
    And each user may have at most one rating per movie
    And a comment is optional

  Scenario Outline: Submit a selected star rating (Normal Flow)
    Given I am logged in as "Hamza"
    And I have not rated "Cars2"
    When I submit a selection of <stars> stars for "Cars2" without a comment
    Then my rating is saved
    And the displayed rating shows <stars> stars

    Examples:
    | stars |
    | 1 |
    | 2 |
    | 3 |
    | 4 |
    | 5 |

  Scenario: Submit a rating with a comment (Alternative Flow)
    Given I am logged in as "Hamza"
    And I have not rated "Cars2"
    When I submit 4 selected stars with the comment "Great movie"
    Then my rating and comment are saved
    And the displayed rating is 4 stars with the comment "Great movie"

  Scenario: Update an existing rating (Normal Flow)
    Given I am logged in as "Hamza"
    And my saved rating for "Cars2" is 3 stars with the comment "Good"
    When I submit an updated selection of 5 stars with the comment "Excellent"
    Then my existing rating is updated
    And the displayed rating is 5 stars with the comment "Excellent"

  Scenario: Submit without selecting stars (Error Flow)
    Given I am logged in as "Hamza"
    And I have not rated "Cars2"
    When I submit a review without selecting a star rating
    Then I am informed that a star rating is required
    And no rating or review is saved

  Scenario: Require login to submit a rating (Error Flow)
    Given I am not logged in
    When I attempt to submit a rating for "Cars2"
    Then I am informed that login is required
    And no rating is saved

  Scenario: Handle a failed save (Error Flow)
    Given I am logged in as "Hamza"
    And my saved rating for "Cars2" is 3 stars
    And the rating service is unavailable
    When I submit an update to 5 stars
    Then I am informed that the rating could not be saved
    And the interface does not report a successful save
    And my saved rating remains 3 stars
