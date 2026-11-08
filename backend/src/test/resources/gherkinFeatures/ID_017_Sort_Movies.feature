Feature: Sort Movies
  As a movie recommendation app user
  I would like to sort movies by title, rating, views, or release year in both directions
  So that I can locate specific content relevant to my interests

  Background:
    Given the following movies exist in the catalog:
      | title             | director          | actors                       | genre   | rating | views     | release_year  |
      | Inception         | Christopher Nolan | Leonardo DiCaprio, Tom Hardy | Sci-Fi  | 8.8    | 600000    | 2010          |
      | Interstellar      | Christopher Nolan | Matthew McConaughey          | Sci-Fi  | 8.7    | 550000    | 2014          |
      | The Dark Knight   | Christopher Nolan | Christian Bale, Heath Ledger | Action  | 9.0    | 700000    | 2008          |
      | Pulp Fiction      | Quentin Tarantino | John Travolta, Samuel Jackson| Crime   | 8.9    | 480000    | 1994          |
      | Django Unchained  | Quentin Tarantino | Jamie Foxx, Leonardo DiCaprio| Western | 8.4    | 520000    | 2012          |


  Scenario Outline: Sort movies by individual attributes in ascending and descending order (Normal Flow)
    When the user sorts the movies with <criterion> in <direction> order
    Then the sorted results should be:
    | title             |
    | <first_movie>     |
    | <second_movie>    |
    | <third_movie>     |
    | <fourth_movie>    |
    | <fifth_movie>     |

    Examples:
      | criterion       | direction     | first_movie       | second_movie      | third_movie   | fourth_movie      | fifth_movie       |
      | title           | ascending     | Django Unchained  | Inception         | Interstellar  | Pulp Fiction      | The Dark Knight   |
      | title           | descending    | The Dark Knight   | Pulp Fiction      | Interstellar  | Inception         | Django Unchained  | 
      | rating          | ascending     | Django Unchained  | Interstellar      | Inception     | Pulp Fiction      | The Dark Knight   |
      | rating          | descending    | The Dark Knight   | Pulp Fiction      | Inception     | Interstellar      | Django Unchained  |   
      | views           | ascending     | Pulp Fiction      | Django Unchained  | Interstellar  | Inception         | The Dark Knight   |
      | views           | descending    | The Dark Knight   | Inception         | Interstellar  | Django Unchained  | Pulp Fiction      |
      | release_year    | ascending     | Pulp Fiction      | The Dark Knight   | Inception     | Django Unchained  | Interstellar      |
      | release_year    | descending    | Interstellar      | Django Unchained  | Inception     | The Dark Knight   | Pulp Fiction      |



  Scenario: Sort by one criterion and then by another criterion (Alternate Flow)
    Given the movie list is currently sorted by title is ascending order
    When the user sorts the movies by rating in descending order
    Then the sorted results should be:
      | title           |
      | The Dark Knight     |
      | Pulp Fiction        |
      | Inception           |
      | Interstellar        |
      | Django Unchained    |



  Scenario: Sort by an unsupported criterion (Error Flow)
    When the user sorts the movies by "duration" in "ascending" order
    Then an error message "Invalid sort criterion" should be issued
