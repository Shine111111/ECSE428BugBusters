package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Unit tests for the user schema (unique email constraint)
class UserRepositoryTest {
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    // check that a user can be saved and retrieved by email
    @Test
    void saveAndFindUser() {
        User user = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(user);
        assertSame(user, repository.findByEmail("john.doe@gmail.com"));
    }

    // check that a user that does not exist returns null
    @Test
    void findUnknownUserReturnsNull() {
        assertNull(repository.findByEmail("nobody@gmail.com"));
    }

    //check that a user cannot be saved with an email that is already registered
    @Test
    void rejectDuplicateEmail() {
        User first = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(first);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User("0002", "Other", "john.doe@gmail.com", "Other123#")));
        assertEquals("Email already registered", e.getMessage());
        assertSame(first, repository.findByEmail("john.doe@gmail.com"));
    }
}
