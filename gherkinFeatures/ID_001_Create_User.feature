Feature: Create User

As a new visitor of the app
I want to create a user account with my email and a password
So that I can then login with my credentials and access to the main page

Scenario Outline: New user successfully creates an account (Normal Flow)

        Given no account exists with the email <email>
        When a user submits <name>, <email> and <password> to create an account
        Then a new <user_id>, <name>, <email> and initial <password> are generated.
| name              | email                 | password      | user_id |
| John Doe          | john.doe@gmail.com    | Quack123#     | 0001    |
| John Dough        | john.dough@gmail.com  | Dough123#     | 0002    |
| John Duck         | john.duck@gmail.com   | Duck123#      | 0003    |

Scenario Outline: User attempts to register with an email that already exists in the system (Error Flow)

        Given an account already exists with email <email>
        When a user submits <name>, <email>, and <password> to create an account
        Then an "Email already registered" message is shown
        And no new account is created

| name              | email                 | password  |
| John Doe          | john.doe@gmail.com    | Quack123# |

Scenario Outline: User attempts to register with an invalid email format (Error Flow)

        Given no account exists with the email <email>
        When a user submits <name>, <email>, and <password> to create an account
        Then an "Invalid email format" message is shown
        And no new account is created

| name              | email                 | password  |
| John Duck         | john.duck.gmail.com   | Duck123#  |

Scenario Outline: User attempts to register with a password that does not meet requirements (Error Flow)

        Given no account exists with the email <email>
        When a user submits <name>, <email>, and <password> to create an account
        Then a "Password does not meet requirements" message is shown
        And no new account is created

| name              | email                 | password |
| John Dough        | john.dough@gmail.com  | Quack    |
