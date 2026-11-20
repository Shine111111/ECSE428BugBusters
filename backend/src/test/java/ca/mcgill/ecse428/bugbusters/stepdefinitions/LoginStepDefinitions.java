package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ca.mcgill.ecse428.bugbusters.dto.LoginRequest;
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

// TID022 - step definitions for ID_002_User_Login.feature.
// Logins are sent to the POST /users/login endpoint (TID020), like CreateUserStepDefinitions does for POST /users.
// "no account exists with the email ..." is defined in CreateUserStepDefinitions and
// "a/an "..." message is shown" in MessageStepDefinitions. The users table is reset by TestDatabaseHooks.
public class LoginStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageStepDefinitions messages;

    private MockHttpServletResponse response;
    private JsonNode responseBody;

    @Given("an account exists with email {word} and password {word}")
    public void anAccountExistsWithEmailAndPassword(String email, String password) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            userService.createUser("Test User", email, password);
        }
        User account = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertEquals(password, account.getPassword(), "An account with this email exists with another password");
    }

    @When("a user submits {word} and {word} to log in")
    public void aUserSubmitsToLogIn(String email, String password) throws Exception {
        logIn(email, password);
    }

    @When("a user submits a blank email and {word} to log in")
    public void aUserSubmitsABlankEmailToLogIn(String password) throws Exception {
        logIn("", password);
    }

    @When("a user submits {word} and a blank password to log in")
    public void aUserSubmitsABlankPasswordToLogIn(String email) throws Exception {
        logIn(email, "");
    }

    @Then("the user is logged in to the account with email {word}")
    public void theUserIsLoggedInToTheAccountWithEmail(String email) {
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "Login failed: " + responseBody);
        assertEquals(email, responseBody.get("email").asText());
        assertFalse(responseBody.has("password"), "The API must not return the password");
        User account = userRepository.findById(responseBody.get("userId").asText()).orElseThrow();
        assertEquals(email, account.getEmail());
    }

    @Then("the user is not logged in")
    public void theUserIsNotLoggedIn() {
        assertTrue(HttpStatus.valueOf(response.getStatus()).isError(), "Expected the login to be rejected");
        assertFalse(responseBody.has("userId"));
    }

    // Sends the credentials to POST /users/login (TID020), the same way the frontend will call it
    private void logIn(String email, String password) throws Exception {
        String json = objectMapper.writeValueAsString(new LoginRequest(email, password));
        response = mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn()
                .getResponse();
        responseBody = objectMapper.readTree(response.getContentAsString());
        boolean rejected = HttpStatus.valueOf(response.getStatus()).isError();
        messages.shownError(rejected ? responseBody.get("error").asText() : null);
    }
}
