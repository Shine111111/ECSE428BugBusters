package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ca.mcgill.ecse428.bugbusters.dto.CreateUserRequest;
import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

// Step definitions for ID_001_Create_User.feature.
// Accounts are created through the POST /users endpoint, the same way the frontend will call it.
// The users table is reset by TestDatabaseHooks.
public class CreateUserStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private MockHttpServletResponse response;
    private JsonNode responseBody;
    private long userCountBeforeSubmit;

    // ---- Shared steps ----

    @Given("no account exists with the email {word}")
    public void noAccountExistsWithTheEmail(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(userRepository::delete);
        assertFalse(userRepository.existsByEmailIgnoreCase(email));
    }

    // Matches both "<name>, <email> and <password>" and "<name>, <email>, and <password>"
    @When("^a user submits (.+), ([^\\s,]+),? and (\\S+) to create an account$")
    public void aUserSubmitsToCreateAnAccount(String name, String email, String password) throws Exception {
        userCountBeforeSubmit = userRepository.count();
        String json = objectMapper.writeValueAsString(new CreateUserRequest(name, email, password));
        response = mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn()
                .getResponse();
        responseBody = objectMapper.readTree(response.getContentAsString());
    }

    // ---- TID018 - Normal flow ----

    @Then("^a new (\\d+), (.+), ([^\\s,]+) and initial (\\S+) are generated\\.$")
    public void aNewUserIsGenerated(String userId, String name, String email, String password) {
        assertEquals(HttpStatus.CREATED.value(), response.getStatus(),
                "Account creation failed: " + responseBody);
        assertEquals(userId, responseBody.get("userId").asText());
        assertEquals(name, responseBody.get("name").asText());
        assertEquals(email, responseBody.get("email").asText());
        assertFalse(responseBody.has("password"), "The API must not return the password");

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
        assertTrue(HttpStatus.valueOf(response.getStatus()).is4xxClientError(),
                "Expected an error response but got " + response.getStatus());
        assertEquals(expectedMessage, responseBody.get("error").asText());
    }

    @Then("no new account is created")
    public void noNewAccountIsCreated() {
        assertFalse(responseBody.has("userId"));
        assertEquals(userCountBeforeSubmit, userRepository.count());
    }
}
