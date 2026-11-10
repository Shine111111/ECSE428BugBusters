@SID0027
Feature: SID0027 - View trending movies page

  As a logged-in user
  I want to view the movies that are currently trending
  So that I can discover movies that are currently popular

  Background:
    Given the app is open
    And the user is logged in as "viewer@example.com"
    And only movies with at least 30000 votes are eligible to appear on the trending movies page
    And the trending score is calculated as:
      | metric          | contribution             |
      | rating          | rating * 10              |
      | number of votes | number of votes / 10000  |
      | comment count   | comment count / 100      |
      | view count      | view count / 1000        |
    And trending movies are ranked by trending score descending
    And the following movies exist in the catalog:
      | title              | rating | number_of_votes | comment_count | view_count |
      | The Dark Knight    | 9.0    | 700000          | 5000          | 900000     |
      | Inception          | 8.8    | 600000          | 4500          | 850000     |
      | Interstellar       | 8.7    | 550000          | 4200          | 800000     |
      | Django Unchained   | 8.4    | 520000          | 3500          | 700000     |
      | Pulp Fiction       | 8.9    | 480000          | 4000          | 750000     |
      | Hidden Gem         | 9.4    | 20000           | 800           | 100000     |

  Scenario: SID0027 - View the trending movies page (Normal Flow)
    When the user requests to view the trending movies page
    Then the trending movies page should be displayed
    And the trending movies should be ranked by their trending score in descending order

  Scenario: SID0027 - Display movie information on the trending movies page (Normal Flow)
    When the user requests to view the trending movies page
    Then each trending movie should display its title
    And each trending movie should display its rating
    And each trending movie should display its number of votes
    And each trending movie should display its comment count
    And each trending movie should display its view count

  Scenario: SID0027 - Exclude movies below the popularity threshold (Alternative Flow)
    When the user requests to view the trending movies page
    Then "Hidden Gem" should not be included in the trending movies list
    And all displayed movies should have at least 30000 votes

  Scenario: SID0027 - Rank movies using multiple popularity signals (Alternative Flow)
    Given the following additional movies exist in the catalog:
      | title  | rating | number_of_votes | comment_count | view_count |
      | Aurora | 8.5    | 400000          | 2000          | 300000     |
      | Apollo | 8.5    | 400000          | 5000          | 700000     |
    When the user requests to view the trending movies page
    Then "Apollo" should have a higher trending score than "Aurora"
    And "Apollo" should be ranked above "Aurora"

  Scenario: SID0027 - Apply deterministic tie breakers when trending scores match
    Given the following additional movies exist in the catalog:
      | title  | rating | number_of_votes | comment_count | view_count |
      | Aurora | 8.0    | 100000          | 1000          | 100000     |
      | Apollo | 8.0    | 90000           | 1100          | 100000     |
      | Beacon | 8.0    | 90000           | 1100          | 100000     |
      | Comet  | 8.0    | 90000           | 1100          | 100000     |
    When the user requests to view the trending movies page
    Then the trending scores include:
      | title  | score |
      | Aurora | 200.0 |
      | Apollo | 200.0 |
      | Beacon | 200.0 |
      | Comet  | 200.0 |
    And "Aurora" is ranked above "Apollo"
    And "Apollo" is ranked above "Beacon"
    And "Beacon" is ranked above "Comet"

  Scenario: SID0027 - Update the trending order when movie popularity changes (Alternative Flow)
    Given "Pulp Fiction" receives 300000 additional votes
    And "Pulp Fiction" receives 300000 additional views
    When the user requests to view the trending movies page
    Then the trending score for "Pulp Fiction" should increase
    And the trending movies should be reordered using the updated trending scores

  Scenario: SID0027 - View trending movies when the catalog is empty (Error Flow)
    Given there are no movies in the catalog
    When the user requests to view the trending movies page
    Then the trending movies list should be empty
    And an informative message "No trending movies available" should be displayed

  Scenario: SID0027 - Handle movies with missing trending data (Error Flow)
    Given a movie exists with missing trending data
    When the user requests to view the trending movies page
    Then the movie with missing trending data should not be included in the trending movies list
    And the trending movies page should still be displayed