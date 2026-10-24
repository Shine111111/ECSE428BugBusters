package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.service.UserIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

//Unit tests for user ID generation (in-memory H2 database)
@DataJpaTest
@Import(UserIdGenerator.class)
class UserIdGeneratorTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserIdGenerator generator;

    private void addUser(String userId) {
        repository.saveAndFlush(new User(userId, "User " + userId, "user" + userId + "@gmail.com", "Quack123#"));
    }

    @Test
    void firstIdIs0001() {
        assertEquals("0001", generator.nextId());
    }

    @Test
    void nextIdFollowsHighestExistingId() {
        addUser("0001");
        addUser("0002");
        assertEquals("0003", generator.nextId());
    }

    @Test
    void nextIdSkipsGapsAndUsesHighestId() {
        addUser("0001");
        addUser("0007");
        assertEquals("0008", generator.nextId());
    }

    @Test
    void idsKeepGrowingPast9999() {
        addUser("9999");
        assertEquals("10000", generator.nextId());
        addUser("10000");
        assertEquals("10001", generator.nextId());
    }
}
