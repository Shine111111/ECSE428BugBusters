Feature: Search Movies
  As a movie recommendation app user
  I would like to search for movies by name, actor, director, or genre
  So that I can locate specific content relevant to my interests

  Background:
    Given the following movies exist in the catalog:
      | title             | director          | actors                       | genre   |
      | Inception         | Christopher Nolan | Leonardo DiCaprio, Tom Hardy | Sci-Fi  |
      | Interstellar      | Christopher Nolan | Matthew McConaughey          | Sci-Fi  |
      | The Dark Knight   | Christopher Nolan | Christian Bale, Heath Ledger | Action  |
      | Pulp Fiction      | Quentin Tarantino | John Travolta, Samuel Jackson| Crime   |
      | Django Unchained  | Quentin Tarantino | Jamie Foxx, Leonardo DiCaprio| Western |



  Scenario Outline: Search movies by individual attributes (Normal Flow)
    When the user searches for movies with <criterion> matching "<query>"
    Then the search results should contain the movie "<expected_title>"

    Examples:
      | criterion | query             | expected_title   |
      | title     | Inception         | Inception        |
      | director  | Quentin Tarantino | Pulp Fiction     |
      | actor     | Leonardo DiCaprio | Django Unchained |
      | genre     | Sci-Fi            | Interstellar     |



  Scenario: Search query matching multiple movies (Alternate Flow)
    When the user searches for movies with director matching "Christopher Nolan"
    Then the search results should contain 3 movies
    And the results should include:
      | title           |
      | Inception       |
      | Interstellar    |
      | The Dark Knight |



  Scenario: Search query with no matching records (Error Flow)
    When the user searches for movies with title matching "Unknown Odyssey"
    Then the search result list should be empty
    And an informative message "No movies found matching your criteria" should be displayed

  Scenario: Search with blank query string (Error Flow)
    When the user submits an empty search query
    Then an error message "Search query cannot be empty" should be issued