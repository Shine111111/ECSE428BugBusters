Feature: Get personalized movie recommendations

  As a logged-in user
  I want to receive movie recommendations based on my preferences, ratings, and movie popularity
  So that I can discover movies I am likely to enjoy

  Background:
    Given the app is open
    And the user is logged in as "viewer@example.com"
    And only unwatched movies are eligible for recommendation
    And the recommendation scoring rules are:
      | condition                                                           | score change |
      | each matching favorite genre                                        | +30          |
      | each matching favorite actor                                        | +25          |
      | each matching favorite director                                     | +25          |
      | shares a genre with a movie the user rated 4 or 5 stars             | +15          |
      | shares an actor or director with a movie the user rated 4 or 5 stars | +10          |
      | shares a genre with a movie the user rated 1 or 2 stars             | -20          |
      | shares an actor or director with a movie the user rated 1 or 2 stars | -15          |
    And the popularity bonus is calculated as:
      | metric         | formula                         |
      | average rating | average rating multiplied by 2  |
      | rating count   | min(rating count, 1000) / 100   |
      | comment count  | min(comment count, 500) / 100   |
    And recommendations are sorted by total score descending, average rating descending, view count descending, and title ascending
    And the app has the following movies available:
      | title               | genres                 | actors                     | director          | average rating | rating count | comment count | view count |
      | Arrival             | Science Fiction, Drama | Amy Adams, Jeremy Renner   | Denis Villeneuve  | 4.6            | 900          | 200           | 4500       |
      | Blade Runner 2049   | Science Fiction, Drama | Ryan Gosling, Ana de Armas | Denis Villeneuve  | 4.5            | 850          | 300           | 5000       |
      | La La Land          | Musical, Romance       | Ryan Gosling, Emma Stone   | Damien Chazelle   | 4.2            | 700          | 100           | 4200       |
      | Pride and Prejudice | Romance, Drama         | Keira Knightley            | Joe Wright        | 4.1            | 700          | 100           | 3000       |
      | The Dark Knight     | Action, Crime          | Christian Bale             | Christopher Nolan | 4.8            | 2000         | 700           | 9000       |

  Scenario: Rank recommendations using explicit user preferences and popularity
    Given the user's favorite genres include "Science Fiction"
    And the user's favorite actors include "Ryan Gosling"
    And the user's favorite directors include "Denis Villeneuve"
    When the user requests personalized movie recommendations
    Then the recommendation list is displayed
    And the recommendation scores include:
      | title             | score |
      | Blade Runner 2049 | 100.5 |
      | Arrival           | 75.2  |
      | La La Land        | 41.4  |
    And "Blade Runner 2049" is ranked above "Arrival"
    And "Arrival" is ranked above "La La Land"

  Scenario: Adjust recommendations based on the user's movie ratings
    Given the user has watched and rated "Arrival" 5 stars
    And the user has watched and rated "Pride and Prejudice" 2 stars
    When the user requests personalized movie recommendations
    Then the recommendation list is displayed
    And the recommendations do not include "Arrival"
    And the recommendations do not include "Pride and Prejudice"
    And the recommendation scores include:
      | title             | score |
      | Blade Runner 2049 | 25.5  |
      | The Dark Knight   | 24.6  |
      | La La Land        | -3.6  |
    And "Blade Runner 2049" is ranked above "The Dark Knight"
    And "The Dark Knight" is ranked above "La La Land"

  Scenario: Use ratings and comments as popularity when the user has no personal signals
    Given the user has no favorite genres
    And the user has no favorite actors
    And the user has no favorite directors
    And the user has no watched movies
    And the user has no movie ratings
    When the user requests personalized movie recommendations
    Then the recommendation list is displayed
    And the recommendation scores include:
      | title             | score |
      | The Dark Knight   | 24.6  |
      | Blade Runner 2049 | 20.5  |
      | Arrival           | 20.2  |
    And "The Dark Knight" is the first recommendation

  Scenario: Do not let popularity override a strong preference match
    Given the user's favorite genres include "Science Fiction"
    And the user has no watched movies
    When the user requests personalized movie recommendations
    Then the recommendation list is displayed
    And "Blade Runner 2049" is ranked above "The Dark Knight"
    And "Arrival" is ranked above "The Dark Knight"

  Scenario: Exclude movies the user has already watched before scoring recommendations
    Given the user's favorite genres include "Science Fiction"
    And the user has watched "Arrival"
    And the user has watched "Blade Runner 2049"
    When the user requests personalized movie recommendations
    Then the recommendation list is displayed
    And the recommendations do not include "Arrival"
    And the recommendations do not include "Blade Runner 2049"
    And "The Dark Knight" is the first recommendation

  Scenario: Apply deterministic tie breakers when recommendation scores match
    Given the user has no favorite genres
    And the user has no favorite actors
    And the user has no favorite directors
    And the app also has the following unwatched movies available:
      | title  | genres  | actors  | director   | average rating | rating count | comment count | view count |
      | Aurora | Mystery | Actor A | Director A | 4.6            | 80           | 100           | 1000       |
      | Apollo | Mystery | Actor B | Director B | 4.5            | 100          | 100           | 2000       |
      | Beacon | Mystery | Actor C | Director C | 4.5            | 100          | 100           | 1500       |
      | Comet  | Mystery | Actor D | Director D | 4.5            | 100          | 100           | 1500       |
    When the user requests personalized movie recommendations
    Then the recommendation scores include:
      | title  | score |
      | Aurora | 11.0  |
      | Apollo | 11.0  |
      | Beacon | 11.0  |
      | Comet  | 11.0  |
    And "Aurora" is ranked above "Apollo"
    And "Apollo" is ranked above "Beacon"
    And "Beacon" is ranked above "Comet"
