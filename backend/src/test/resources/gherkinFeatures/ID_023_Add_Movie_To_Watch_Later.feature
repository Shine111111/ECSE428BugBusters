Feature: ID_023 Add Movie to Watch Later Playlist

  As a user of the app
  I want to add a movie to my Watch Later playlist
  So that I can save and keep track of the movies I want to watch

  Background:
    Given I am logged in as a user
    And the following movies exist in the catalog:
      | title             |
      | Pulp Fiction      |
      | Interstellar      |
      | The Dark Knight   |
      | Inception         |

  Scenario Outline: Add a movie that is not yet in my Watch Later playlist (Normal Flow)

    Given my Watch Later playlist contains the movie with title "Inception"
    And my Watch Later playlist does not contain the movie with title "<title>"
    When I add the movie with title "<title>" to my Watch Later playlist
    Then the movie with title "<title>" should appear in my Watch Later playlist
    And I should see the message "Added to Watch Later"

    Examples:
      | title              |
      | Pulp Fiction       |
      | Interstellar       |
      | The Dark Knight    |

  Scenario Outline: Add a movie to an empty Watch Later playlist (Alternate Flow)

    Given my Watch Later playlist is empty
    When I add the movie with title "<title>" to my Watch Later playlist
    Then the movie with title "<title>" should appear in my Watch Later playlist
    And I should see the message "You have added your first movie to your Watch Later Playlist"

    Examples:
      | title              |
      | Pulp Fiction       |
      | Interstellar       |
      | The Dark Knight    |

  Scenario Outline: Attempt to add a movie that is already in my Watch Later playlist (Error Flow)

    Given my Watch Later playlist contains the movie with title "<title>"
    When I add the movie with title "<title>" to my Watch Later playlist
    Then I should see the error message "Movie already added"
    And my Watch Later playlist should remain unchanged

    Examples:
      | title              |
      | Pulp Fiction       |
      | Interstellar       |
      | The Dark Knight    |
