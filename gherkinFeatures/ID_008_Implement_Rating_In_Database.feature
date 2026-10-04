Feature: Implement rating in database

As a registered user
I want my movie ratings and optional comments to be stored reliably
So that I can retrive my opinions on movies later

Background: 
    Given a registered user "Hamza" exists
    And a movie named "Cars2" exists

Scenario Outline: Store a valid rating without a comment (Normal Flow)
    Given "Hamza" has no stored rating for "Cars2"
    When a rating of <stars> stars without a comment is saved for "Hamza" and "Cars2"
    Then the stored rating for "Hamza" and "Cars2" has <stars> stars
    And the stored rating has no comment 

    Examples:
      | stars |
      | 1     |
      | 2     |
      | 3     |
      | 4     |
      | 5     |

Scenario: Store a rating with an optional comment (Alternative Flow)
    Given "Hamza" has no stored rating for "Cars2"
    When a rating of 4 stars with the comment "Great movie" is saved for "Hamza" and "Cars2"
    Then the stored rating for "Hamza" and "Cars2" has 4 stars
    And the stored comment is "Great movie"

Scenario: Keep different users' ratings separate (Normal Flow)
    Given a registered user "Robert" exists
    And "Hamza" has a stored rating of 4 stars for "Cars2"
    When a rating of 2 stars is saved for "Robert" and "Cars2"
    Then the stored rating for "Hamza" and "Cars2" has 4 stars
    And the stored rating for "Robert" and "Cars2" has 2 stars

  Scenario Outline: Reject an invalid rating value (Error Flow)
    Given "Hamza" has no stored rating for "Cars2"
    When a rating with the value <rating> is saved for "Hamza" and "Cars2"
    Then the save is rejected because the rating must be a whole number from 1 to 5
    And no rating is stored for "Hamza" and "Cars2"

    Examples:
      | rating |
      | 0      |
      | 6      |
      | -1     |
      | 2.5    |
      | abc    |
      |        |

  Scenario: Reject a rating for a nonexistent movie (Error Flow)
    Given no movie with identifier "UNKNOWN_MOVIE" exists
    When a rating of 4 stars is saved for "Hamza" and movie "UNKNOWN_MOVIE"
    Then the save is rejected because the movie does not exist
    And no rating is stored for "Hamza" and movie "UNKNOWN_MOVIE"

  Scenario: Reject a rating for a nonexistent user (Error Flow)
    Given no user with identifier "UNKNOWN_USER" exists
    When a rating of 4 stars is saved for user "UNKNOWN_USER" and "Cars2"
    Then the save is rejected because the user does not exist
    And no rating is stored for user "UNKNOWN_USER" and "Cars2"
