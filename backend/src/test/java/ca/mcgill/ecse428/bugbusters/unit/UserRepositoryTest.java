package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

//Unit tests for the user repository / users table (unique email constraint)
//Runs against an in-memory H2 database (see src/test/resources/application.properties)
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    // saves and flushes, then clears the persistence context so reads come from the database
    private void persist(User user) {
        repository.saveAndFlush(user);
        entityManager.clear();
    }

    @Test
    void savedUserCanBeFoundByEmail() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        User found = repository.findByEmailIgnoreCase("john.doe@gmail.com").orElseThrow();
        assertEquals("0001", found.getUserId());
        assertEquals("John Doe", found.getName());
        assertEquals("john.doe@gmail.com", found.getEmail());
        assertEquals("Quack123#", found.getPassword());
    }

    @Test
    void savedUserCanBeFoundById() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        assertEquals("john.doe@gmail.com", repository.findById("0001").orElseThrow().getEmail());
    }

    @Test
    void emailIsStoredTrimmedAndLowercased() {
        persist(new User("0001", "John Doe", "  John.Doe@GMAIL.com ", "Quack123#"));
        assertEquals("john.doe@gmail.com", repository.findById("0001").orElseThrow().getEmail());
    }

    @Test
    void findByEmailIgnoresCase() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        assertEquals("0001", repository.findByEmailIgnoreCase("John.Doe@GMAIL.com").orElseThrow().getUserId());
        assertTrue(repository.existsByEmailIgnoreCase("JOHN.DOE@gmail.com"));
    }

    @Test
    void findByUnknownEmailReturnsEmpty() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        assertTrue(repository.findByEmailIgnoreCase("john.duck@gmail.com").isEmpty());
        assertFalse(repository.existsByEmailIgnoreCase("john.duck@gmail.com"));
    }

    @Test
    void findByUnknownIdReturnsEmpty() {
        assertTrue(repository.findById("9999").isEmpty());
    }

    @Test
    void emptyRepositoryHasNoUsers() {
        assertEquals(0, repository.count());
        assertFalse(repository.existsByEmailIgnoreCase("john.doe@gmail.com"));
    }

    @Test
    void savesMultipleUsersWithDistinctEmails() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        persist(new User("0002", "John Dough", "john.dough@gmail.com", "Dough123#"));
        persist(new User("0003", "John Duck", "john.duck@gmail.com", "Duck123#"));
        assertEquals(3, repository.count());
        assertTrue(repository.existsByEmailIgnoreCase("john.doe@gmail.com"));
        assertTrue(repository.existsByEmailIgnoreCase("john.dough@gmail.com"));
        assertTrue(repository.existsByEmailIgnoreCase("john.duck@gmail.com"));
    }

    @Test
    void rejectsSecondUserWithSameEmail() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User("0002", "Johnny", "john.doe@gmail.com", "Other123#")));
        entityManager.clear();
        assertEquals(1, repository.count());
        assertTrue(repository.findById("0002").isEmpty());
        assertEquals("0001", repository.findByEmailIgnoreCase("john.doe@gmail.com").orElseThrow().getUserId());
    }

    @Test
    void rejectsSecondUserWithSameEmailInDifferentCase() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User("0002", "Johnny", "JOHN.DOE@gmail.com", "Other123#")));
        entityManager.clear();
        assertEquals(1, repository.count());
    }

    @Test
    void resavingSameUserUpdatesIt() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        User user = repository.findById("0001").orElseThrow();
        user.setName("Johnathan Doe");
        persist(user);
        assertEquals(1, repository.count());
        assertEquals("Johnathan Doe", repository.findById("0001").orElseThrow().getName());
    }

    @Test
    void changingEmailFreesOldEmail() {
        persist(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));
        persist(new User("0001", "John Doe", "john.d@gmail.com", "Quack123#"));
        assertTrue(repository.findByEmailIgnoreCase("john.doe@gmail.com").isEmpty());
        assertEquals("0001", repository.findByEmailIgnoreCase("john.d@gmail.com").orElseThrow().getUserId());
        persist(new User("0002", "John Dough", "john.doe@gmail.com", "Dough123#"));
        assertEquals("0002", repository.findByEmailIgnoreCase("john.doe@gmail.com").orElseThrow().getUserId());
        assertEquals(2, repository.count());
    }

    @Test
    void rejectsNullUserId() {
        assertThrows(DataAccessException.class,
                () -> repository.saveAndFlush(new User(null, "John Doe", "john.doe@gmail.com", "Quack123#")));
    }

    @Test
    void rejectsNullEmail() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User("0001", "John Doe", null, "Quack123#")));
    }

    @Test
    void rejectsNullName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User("0001", null, "john.doe@gmail.com", "Quack123#")));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User("0001", "John Doe", "john.doe@gmail.com", null)));
    }
}
