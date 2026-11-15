package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Then;

// Shared step for the error message shown to the user, used by ID_001 (Create User) and
// ID_002 (User Login). The When steps of those features call shownError(...) with the error the
// app returned, or with null when the action succeeded.
// Cucumber creates one instance per scenario, so the message never leaks between scenarios.
public class MessageStepDefinitions {

    private String shownErrorMessage;

    public void shownError(String errorMessage) {
        shownErrorMessage = errorMessage;
    }

    @Then("^an? \"([^\"]*)\" message is shown$")
    public void aMessageIsShown(String expectedMessage) {
        assertEquals(expectedMessage, shownErrorMessage);
    }
}
