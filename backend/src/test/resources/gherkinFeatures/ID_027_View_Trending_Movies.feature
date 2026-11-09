@SID0027
Feature: SID0027 - View trending movies page

  As a logged-in user
  I want to view the movies that are currently trending
  So that I can discover popular and highly rated movies

  Background:
    Given the app is open
    And the user is logged in as "viewer@example.com"
    And the following movies exist in the catalog:
      | title              | rating | number_of_votes |
      | The Dark Knight    | 9.0    | 700000          |
      | Inception          | 8.8    | 600000          |
      | Interstellar       | 8.7    | 550000          |
      | Django Unchained   | 8.4    | 520000          |
      | Pulp Fiction       | 8.9    | 480000          |
    And trending movies are ranked by number of votes descending
    And movies with the same number of votes are ranked by rating descending

  Scenario: SID0027 - View the trending movies page (Normal Flow)
    When the user requests to view the trending movies page
    Then the trending movies page should be displayed
    And the trending movies should be displayed in the following order:
      | title            |
      | The Dark Knight  |
      | Inception        |
      | Interstellar     |
      | Django Unchained |
      | Pulp Fiction     |

  Scenario: SID0027 - Display movie information on the trending movies page (Normal Flow)
    When the user requests to view the trending movies page
    Then each trending movie should display its title
    And each trending movie should display its rating
    And each trending movie should display its number of votes

  Scenario: SID0027 - Rank movies with the same popularity by rating (Alternative Flow)
    Given the following additional movies exist in the catalog:
      | title  | rating | number_of_votes |
      | Aurora | 8.7    | 400000          |
      | Apollo | 9.1    | 400000          |
    When the user requests to view the trending movies page
    Then "Apollo" should be ranked above "Aurora"

  Scenario: SID0027 - Update the trending order when movie popularity changes (Alternative Flow)
    Given "Pulp Fiction" has 750000 votes
    When the user requests to view the trending movies page
    Then "Pulp Fiction" should be the first trending movie
    And "The Dark Knight" should be ranked below "Pulp Fiction"

  Scenario: SID0027 - View trending movies when the catalog is empty (Error Flow)
    Given there are no movies in the catalog
    When the user requests to view the trending movies page
    Then the trending movies list should be empty
    And an informative message "No trending movies available" should be displayed

  Scenario: SID0027 - Handle movies with missing trending data (Error Flow)
    Given a movie exists with missing rating or number of votes
    When the user requests to view the trending movies page
    Then the movie with missing trending data should not be included in the trending movies list
    And the trending movies page should still be displayed