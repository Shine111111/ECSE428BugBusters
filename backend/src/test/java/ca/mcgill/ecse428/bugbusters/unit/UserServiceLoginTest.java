package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.service.UserIdGenerator;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TID014 - login success cases (repository is mocked)
@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserIdGenerator idGenerator; // not used by login, but needed by the constructor

    private UserService service;

    // the account already saved in the "database"
    private final User john = new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#");

    @BeforeEach
    void setUp() {
        service = new UserService(repository, idGenerator);
    }

    // right email + right password -> we get John back
    @Test
    void logsInWithValidCredentials() {
        when(repository.findByEmailIgnoreCase("john.doe@gmail.com")).thenReturn(Optional.of(john));
        User loggedIn = service.login("john.doe@gmail.com", "Quack123#");
        assertSame(john, loggedIn);
    }

    // caps or extra spaces in the email still work
    @ParameterizedTest
    @ValueSource(strings = {"John.Doe@Gmail.com", "JOHN.DOE@GMAIL.COM", "  john.doe@gmail.com  ", " John.Doe@gmail.com\t"})
    void logsInWithDifferentCaseOrSurroundingSpaces(String submittedEmail) {
        when(repository.findByEmailIgnoreCase("john.doe@gmail.com")).thenReturn(Optional.of(john));
        User loggedIn = service.login(submittedEmail, "Quack123#");
        assertSame(john, loggedIn);
    }

    // the repository gets the cleaned-up email, not the raw one
    @Test
    void looksUpUserByNormalizedEmail() {
        when(repository.findByEmailIgnoreCase("john.doe@gmail.com")).thenReturn(Optional.of(john));
        service.login("  John.Doe@GMAIL.com ", "Quack123#");
        verify(repository).findByEmailIgnoreCase("john.doe@gmail.com");
    }
}
