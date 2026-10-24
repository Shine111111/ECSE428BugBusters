package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;

// Step definitions for ID_001_Create_User.feature
public class CreateUserStepDefinitions {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // The normal flow examples expect IDs 0001, 0002, 0003 in order, so users are only
    // wiped when a new feature file starts, not before every scenario
    private static URI currentFeature;

    private User createdUser;
    private String errorMessage;
    private long userCountBeforeSubmit;

    @Before
    public void resetUsersWhenFeatureStarts(Scenario scenario) {
        if (!scenario.getUri().equals(currentFeature)) {
            userRepository.deleteAll();
            currentFeature = scenario.getUri();
        }
    }

    // ---- Shared steps ----

    @Given("no account exists with the email {word}")
    public void noAccountExistsWithTheEmail(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(userRepository::delete);
        assertFalse(userRepository.existsByEmailIgnoreCase(email));
    }

    // Matches both "<name>, <email> and <password>" and "<name>, <email>, and <password>"
    @When("^a user submits (.+), ([^\\s,]+),? and (\\S+) to create an account$")
    public void aUserSubmitsToCreateAnAccount(String name, String email, String password) {
        userCountBeforeSubmit = userRepository.count();
        try {
            createdUser = userService.createUser(name, email, password);
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    // ---- TID018 - Normal flow ----

    @Then("^a new (\\d+), (.+), ([^\\s,]+) and initial (\\S+) are generated\\.$")
    public void aNewUserIsGenerated(String userId, String name, String email, String password) {
        assertNull(errorMessage, "Account creation failed: " + errorMessage);
        assertEquals(userId, createdUser.getUserId());
        User stored = userRepository.findById(userId).orElseThrow();
        assertEquals(name, stored.getName());
        assertEquals(email, stored.getEmail());
        assertEquals(password, stored.getPassword());
        assertEquals(userCountBeforeSubmit + 1, userRepository.count());
    }

    // ---- TID019 - Error flows ----

    @Given("an account already exists with email {word}")
    public void anAccountAlreadyExistsWithEmail(String email) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            userService.createUser("Existing User", email, "Existing123#");
        }
        assertTrue(userRepository.existsByEmailIgnoreCase(email));
    }

    @Then("^an? \"([^\"]*)\" message is shown$")
    public void aMessageIsShown(String expectedMessage) {
        assertEquals(expectedMessage, errorMessage);
    }

    @Then("no new account is created")
    public void noNewAccountIsCreated() {
        assertNull(createdUser);
        assertEquals(userCountBeforeSubmit, userRepository.count());
    }
}
