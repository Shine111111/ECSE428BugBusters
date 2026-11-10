Feature: SID0002 - User Login

As a registered user of the app
I want to log in with my email and password
So that I can access the main page

Scenario Outline: Registered user logs in with valid credentials (Normal Flow)

        Given an account exists with email <email> and password <password>
        When a user submits <email> and <password> to log in
        Then the user is logged in to the account with email <email>

        Examples:
        | email                 | password      |
        | john.doe@gmail.com    | Quack123#     |
        | john.dough@gmail.com  | Dough123#     |

Scenario Outline: Registered user logs in using a different letter case in the email (Alternate Flow)

        Given an account exists with email <email> and password <password>
        When a user submits <submitted_email> and <password> to log in
        Then the user is logged in to the account with email <email>

        Examples:
        | email                 | submitted_email       | password      |
        | john.doe@gmail.com    | John.Doe@Gmail.com    | Quack123#     |

Scenario Outline: User attempts to log in with an incorrect password (Error Flow)

        Given an account exists with email <email> and password <password>
        When a user submits <email> and <wrong_password> to log in
        Then an "Invalid email or password" message is shown
        And the user is not logged in

        Examples:
        | email                 | password      | wrong_password |
        | john.doe@gmail.com    | Quack123#     | Wrong123#      |
        | john.doe@gmail.com    | Quack123#     | quack123#      |

Scenario Outline: User attempts to log in with an email that is not registered (Error Flow)

        Given no account exists with the email <email>
        When a user submits <email> and <password> to log in
        Then an "Invalid email or password" message is shown
        And the user is not logged in

        Examples:
        | email                 | password      |
        | jane.doe@gmail.com    | Quack123#     |

Scenario Outline: User attempts to log in with an invalid email format (Error Flow)

        When a user submits <email> and <password> to log in
        Then an "Invalid email format" message is shown
        And the user is not logged in

        Examples:
        | email                 | password      |
        | john.doe.gmail.com    | Quack123#     |

Scenario Outline: User attempts to log in with a blank email (Error Flow)

        When a user submits a blank email and <password> to log in
        Then an "Email is required" message is shown
        And the user is not logged in

        Examples:
        | password      |
        | Quack123#     |

Scenario Outline: User attempts to log in with a blank password (Error Flow)

        When a user submits <email> and a blank password to log in
        Then a "Password is required" message is shown
        And the user is not logged in

        Examples:
        | email                 |
        | john.doe@gmail.com    |
