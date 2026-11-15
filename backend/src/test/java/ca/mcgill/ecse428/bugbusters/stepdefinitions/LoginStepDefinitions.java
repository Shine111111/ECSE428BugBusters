package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

// TID022 - step definitions for ID_002_User_Login.feature.
// Logins go through UserService.login (TID013). Once the login endpoint exists (TID020), the
// When steps can send the request to it instead, as CreateUserStepDefinitions does for POST /users.
// "no account exists with the email ..." is defined in CreateUserStepDefinitions and
// "a/an "..." message is shown" in MessageStepDefinitions. The users table is reset by TestDatabaseHooks.
public class LoginStepDefinitions {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageStepDefinitions messages;

    private User loggedInUser;

    @Given("an account exists with email {word} and password {word}")
    public void anAccountExistsWithEmailAndPassword(String email, String password) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            userService.createUser("Test User", email, password);
        }
        User account = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertEquals(password, account.getPassword(), "An account with this email exists with another password");
    }

    @When("a user submits {word} and {word} to log in")
    public void aUserSubmitsToLogIn(String email, String password) {
        logIn(email, password);
    }

    @When("a user submits a blank email and {word} to log in")
    public void aUserSubmitsABlankEmailToLogIn(String password) {
        logIn("", password);
    }

    @When("a user submits {word} and a blank password to log in")
    public void aUserSubmitsABlankPasswordToLogIn(String email) {
        logIn(email, "");
    }

    @Then("the user is logged in to the account with email {word}")
    public void theUserIsLoggedInToTheAccountWithEmail(String email) {
        assertNotNull(loggedInUser, "Login failed");
        assertEquals(email, loggedInUser.getEmail());
        assertTrue(userRepository.existsById(loggedInUser.getUserId()));
    }

    @Then("the user is not logged in")
    public void theUserIsNotLoggedIn() {
        assertNull(loggedInUser);
    }

    private void logIn(String email, String password) {
        loggedInUser = null;
        try {
            loggedInUser = userService.login(email, password);
            messages.shownError(null);
        } catch (IllegalArgumentException e) {
            messages.shownError(e.getMessage());
        }
    }
}
