package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

//Unit tests for the user repository / user schema (unique email constraint)
class UserRepositoryTest {
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void savedUserCanBeFoundByEmail() {
        User user = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(user);
        User found = repository.findByEmail("john.doe@gmail.com");
        assertSame(user, found);
        assertEquals("0001", found.getUserId());
        assertEquals("John Doe", found.getName());
        assertEquals("john.doe@gmail.com", found.getEmail());
        assertEquals("Quack123#", found.getPassword());
    }

    @Test
    void savedUserCanBeFoundById() {
        User user = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(user);
        assertSame(user, repository.findById("0001"));
    }

    @Test
    void findByEmailIgnoresCaseAndSurroundingSpaces() {
        User user = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(user);
        assertSame(user, repository.findByEmail("John.Doe@GMAIL.com"));
        assertSame(user, repository.findByEmail("  john.doe@gmail.com "));
    }

    @Test
    void findByUnknownEmailReturnsNull() {
        repository.save(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        assertNull(repository.findByEmail("john.duck@gmail.com"));
        assertFalse(repository.existsByEmail("john.duck@gmail.com"));
    }

    @Test
    void findByNullEmailReturnsNull() {
        assertNull(repository.findByEmail(null));
    }

    @Test
    void findByUnknownIdReturnsNull() {
        assertNull(repository.findById("9999"));
    }

    @Test
    void emptyRepositoryHasNoUsers() {
        assertEquals(0, repository.count());
        assertFalse(repository.existsByEmail("john.doe@gmail.com"));
    }

    @Test
    void savesMultipleUsersWithDistinctEmails() {
        repository.save(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        repository.save(new User("0002", "John Dough", "john.dough@gmail.com", "Dough123#"));
        repository.save(new User("0003", "John Duck", "john.duck@gmail.com", "Duck123#"));
        assertEquals(3, repository.count());
        assertTrue(repository.existsByEmail("john.doe@gmail.com"));
        assertTrue(repository.existsByEmail("john.dough@gmail.com"));
        assertTrue(repository.existsByEmail("john.duck@gmail.com"));
    }

    @Test
    void rejectsSecondUserWithSameEmail() {
        User original = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(original);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User("0002", "Johnny", "john.doe@gmail.com", "Other123#")));
        assertEquals("Email already registered", e.getMessage());
        assertEquals(1, repository.count());
        assertNull(repository.findById("0002"));
        assertSame(original, repository.findByEmail("john.doe@gmail.com"));
    }

    @Test
    void rejectsSecondUserWithSameEmailInDifferentCase() {
        repository.save(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User("0002", "Johnny", "JOHN.DOE@gmail.com", "Other123#")));
        assertEquals("Email already registered", e.getMessage());
        assertEquals(1, repository.count());
    }

    @Test
    void resavingSameUserUpdatesIt() {
        User user = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(user);
        user.setName("Johnathan Doe");
        repository.save(user);
        assertEquals(1, repository.count());
        assertEquals("Johnathan Doe", repository.findById("0001").getName());
    }

    @Test
    void changingEmailFreesOldEmail() {
        User user = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");
        repository.save(user);
        repository.save(new User("0001", "John Doe", "john.d@gmail.com", "Quack123#"));
        assertNull(repository.findByEmail("john.doe@gmail.com"));
        assertEquals("0001", repository.findByEmail("john.d@gmail.com").getUserId());
        repository.save(new User("0002", "John Dough", "john.doe@gmail.com", "Dough123#"));
        assertEquals("0002", repository.findByEmail("john.doe@gmail.com").getUserId());
        assertEquals(2, repository.count());
    }

    @Test
    void rejectsNullUser() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(null));
        assertEquals("User cannot be null", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankUserId(String userId) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User(userId, "John Doe", "john.doe@gmail.com", "Quack123#")));
        assertEquals("User ID cannot be blank", e.getMessage());
        assertEquals(0, repository.count());
    }

    @Test
    void rejectsNullUserId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User(null, "John Doe", "john.doe@gmail.com", "Quack123#")));
        assertEquals("User ID cannot be blank", e.getMessage());
        assertEquals(0, repository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankEmail(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User("0001", "John Doe", email, "Quack123#")));
        assertEquals("Email cannot be blank", e.getMessage());
        assertEquals(0, repository.count());
    }

    @Test
    void rejectsNullEmail() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new User("0001", "John Doe", null, "Quack123#")));
        assertEquals("Email cannot be blank", e.getMessage());
        assertEquals(0, repository.count());
    }
}
