package ca.mcgill.ecse428.bugbusters.stepdefinitions;

import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;

// TID008 - test database setup and reset for the acceptance tests.
// Each feature file starts with an empty users table in the in-memory H2 database. Scenarios in the
// same feature share the table because the ID_001 normal flow examples expect IDs 0001, 0002 and
// 0003 in order. Data a scenario needs (e.g. an existing account) is seeded by its Given steps.
public class TestDatabaseHooks {

    private static URI currentFeature;
    private static int lastScenarioLine;

    @Autowired
    private UserRepository userRepository;

    // A feature starts when the file changes, or when the same file runs again (its line numbers go back
    // to the top), e.g. if a second runner is added that also selects the feature files
    @Before(order = 0)
    public void resetUsersWhenFeatureStarts(Scenario scenario) {
        if (!scenario.getUri().equals(currentFeature) || scenario.getLine() <= lastScenarioLine) {
            userRepository.deleteAll();
            currentFeature = scenario.getUri();
        }
        lastScenarioLine = scenario.getLine();
    }
}
