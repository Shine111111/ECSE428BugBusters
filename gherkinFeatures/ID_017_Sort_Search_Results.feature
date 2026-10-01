Feature: Sort Search Results
  As a movie recommendation app user
  I would like to sort my search results by release year, rating, views, and title in either direction
  So that I can easily discover top-rated, newest, or alphabetically ordered content

  Background:
    Given the following search results are currently displayed:
      | title             | release_year | rating | views   |
      | The Dark Knight   | 2008         | 9.0    | 2700000 |
      | Inception         | 2010         | 8.8    | 2400000 |
      | Django Unchained  | 2012         | 8.4    | 1600000 |
      | Interstellar      | 2014         | 8.7    | 1900000 |



  Scenario Outline: Sort search results by metrics in either direction (Normal Flow)
    When the user sorts the current results by "<sort_key>" in "<direction>" order
    Then the first movie in the results should be "<first_movie>"
    And the last movie in the results should be "<last_movie>"

    Examples:
      | sort_key     | direction  | first_movie      | last_movie       |
      | release_year | ascending  | The Dark Knight  | Interstellar     |
      | release_year | descending | Interstellar     | The Dark Knight  |
      | rating       | descending | The Dark Knight  | Django Unchained |
      | rating       | ascending  | Django Unchained | The Dark Knight  |
      | views        | descending | The Dark Knight  | Django Unchained |
      | views        | ascending  | Django Unchained | The Dark Knight  |



  Scenario: Sort results alphabetically by title A to Z (Alternate Flow)
    When the user sorts the current results by "title" in "ascending" order
    Then the movies should appear in the following order:
      | title            |
      | Django Unchained |
      | Inception        |
      | Interstellar     |
      | The Dark Knight  |



  Scenario: Sort when search results are empty (Error Flow)
    Given there are no movies in the current search results
    When the user attempts to sort the results by "rating" in "descending" order
    Then a warning message "No results available to sort" should be displayed