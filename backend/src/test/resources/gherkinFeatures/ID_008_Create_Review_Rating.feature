Feature: Create a Review/Rating

  As a registered user
  I want to rate a movie and optionally write a review
  So that I can share my opinion of the movie

  Background:
    Given a registered user "Hamza" exists
    And "Hamza" is logged in
    And a movie named "Cars2" exists

  Scenario Outline: Create a rating without review text (Normal Flow)
    Given "Hamza" has not rated "Cars2"
    When "Hamza" submits a rating of <stars> stars for "Cars2" without review text
    Then the rating is created successfully
    And "Hamza"'s rating of "Cars2" has <stars> stars
    And the rating has no review text

    Examples:
      | stars |
      | 1     |
      | 2     |
      | 3     |
      | 4     |
      | 5     |

  Scenario Outline: Create a rating with review text (Alternative Flow)
    Given "Hamza" has not rated "Cars2"
    When "Hamza" submits a rating of <stars> stars for "Cars2" with the review text "<review>"
    Then the rating and review are created successfully
    And "Hamza"'s rating of "Cars2" has <stars> stars
    And "Hamza"'s review of "Cars2" has the text "<review>"

    Examples:
      | stars | review      |
      | 4     | Great movie |

  Scenario Outline: Keep different users' ratings separate (Normal Flow)
    Given a registered user "Robert" exists
    And "Hamza" has rated "Cars2" with <hamza_stars> stars
    And "Hamza" has logged out
    And "Robert" is logged in
    And "Robert" has not rated "Cars2"
    When "Robert" submits a rating of <robert_stars> stars for "Cars2" without review text
    Then "Robert"'s rating is created successfully
    And "Hamza"'s rating of "Cars2" remains <hamza_stars> stars
    And "Robert"'s rating of "Cars2" has <robert_stars> stars

    Examples:
      | hamza_stars | robert_stars |
      | 4           | 2            |

  Scenario Outline: Reject an invalid rating value (Error Flow)
    Given "Hamza" has not rated "Cars2"
    When "Hamza" submits the rating value "<rating>" for "Cars2"
    Then the rating is rejected
    And "Hamza" is informed that the rating must be a whole number from 1 to 5
    And no rating or review by "Hamza" is created for "Cars2"

    Examples:
      | rating |
      | 0      |
      | 6      |
      | -1     |
      | 2.5    |
      | abc    |
      |        |

  Scenario Outline: Reject a rating for a nonexistent movie (Error Flow)
    Given no movie with identifier "<movie_id>" exists
    When "Hamza" submits a rating of 4 stars for movie "<movie_id>"
    Then the rating is rejected because the movie does not exist
    And no rating or review by "Hamza" is created for movie "<movie_id>"

    Examples:
      | movie_id      |
      | UNKNOWN_MOVIE |

  Scenario Outline: Reject a rating from a user who is not logged in (Error Flow)
    Given "Hamza" has not rated "Cars2"
    And "Hamza" has logged out
    When "Hamza" attempts to submit a rating of <stars> stars for "Cars2"
    Then the rating is rejected
    And "Hamza" is prompted to log in
    And no rating or review by "Hamza" is created for "Cars2"

    Examples:
      | stars |
      | 4     |


  Scenario Outline: Reject a duplicate rating for the same movie (Error Flow)
    Given "Hamza" has rated "Cars2" with <original_stars> stars
    And "Hamza"'s review of "Cars2" has the text "<original_review>"
    When "Hamza" submits a new rating of <new_stars> stars for "Cars2" with the review text "<new_review>"
    Then the new rating and review are rejected
    And "Hamza" is informed that he has already rated this movie
    And "Hamza"'s rating of "Cars2" remains <original_stars> stars
    And "Hamza"'s review of "Cars2" still has the text "<original_review>"
    And only one rating by "Hamza" exists for "Cars2"

    Examples:
      | original_stars | original_review | new_stars | new_review |
      | 4              | Great movie     | 2         | Too slow   |