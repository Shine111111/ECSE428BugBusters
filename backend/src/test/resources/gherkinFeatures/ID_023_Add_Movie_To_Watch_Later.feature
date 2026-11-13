Feature: Add movie to Watch Later playlist ID_023

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

  Scenario Outline: Add a movie that is not yet in my Watch Later playlist  (Normal Flow)

    Given my Watch Later playlist does not contain the movie with title "<title>"
    And my Watch Later playlist contains at least one other movie
    When I add the movie with title "<title>" to my Watch Later playlist
    Then the movie with title "<title>" should appear in my Watch Later playlist
    And I should see the message "Added to Watch Later"

    Examples:
      | title              |
      | Pulp Fiction       |
      | Interstellar       |
      | The Dark Knight    |

  Scenario Outline: Add a movie to an empty playlist (Alternate Flow)

    Given my Watch Later playlist is empty
    When I add the movie with title "<title>" to my Watch Later playlist
    Then the movie with title "<title>" should appear in my Watch Later playlist
    And I should see the message "You have added your first movie to your Watch Later Playlist"

    Examples:
      | title              |
      | Pulp Fiction       |
      | Interstellar       |
      | The Dark Knight    |


  Scenario Outline: Attempt to add a movie that already is my playlist (Error Flow)

    Given the movie with title "<title>" already exists in my playlist
    When I add the movie with title "<title>" to my Watch Later playlist
    Then I should see the message "Movie already added"
    And my Watch Later playlist should remain unchanged

    Examples:
      | title              |
      | Pulp Fiction       |
      | Interstellar       |
      | The Dark Knight    |

  Scenario Outline: Attempt to add a movie that does not exist (Error Flow)

    Given the movie with title "<title>" does not exist in the catalog
    When I add the movie with title "<title>" to my Watch Later playlist
    Then I should see the message "Movie not found"
    And my Watch Later playlist should remain unchanged

    Examples:
      | title              |
      | Batwomen5          |
      | Flight Wars        |
