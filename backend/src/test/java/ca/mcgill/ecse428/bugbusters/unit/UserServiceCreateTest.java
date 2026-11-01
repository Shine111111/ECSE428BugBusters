package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import ca.mcgill.ecse428.bugbusters.service.UserIdGenerator;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

//Unit tests for the create-user service (repository and ID generator are mocked)
@ExtendWith(MockitoExtension.class)
class UserServiceCreateTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserIdGenerator idGenerator;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, idGenerator);
    }

    private void stubSuccessfulSave(String generatedId) {
        when(idGenerator.nextId()).thenReturn(generatedId);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertRejected(String expectedMessage, String name, String email, String password) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.createUser(name, email, password));
        assertEquals(expectedMessage, e.getMessage());
        verify(repository, never()).save(any(User.class));
        verifyNoInteractions(idGenerator);
    }

    @Test
    void createsUserWithGeneratedId() {
        stubSuccessfulSave("0001");
        User created = service.createUser("John Doe", "john.doe@gmail.com", "Quack123#");
        assertEquals("0001", created.getUserId());
        assertEquals("John Doe", created.getName());
        assertEquals("john.doe@gmail.com", created.getEmail());
        assertEquals("Quack123#", created.getPassword());
    }

    @Test
    void savesTheCreatedUser() {
        stubSuccessfulSave("0002");
        User created = service.createUser("John Dough", "john.dough@gmail.com", "Dough123#");
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertSame(created, saved.getValue());
    }

    @Test
    void lowercasesEmailAndTrimsName() {
        stubSuccessfulSave("0003");
        User created = service.createUser("  John Duck ", "John.Duck@GMAIL.com", "Duck123#");
        assertEquals("John Duck", created.getName());
        assertEquals("john.duck@gmail.com", created.getEmail());
    }

    @Test
    void rejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("john.doe@gmail.com")).thenReturn(true);
        assertRejected("Email already registered", "John Doe", "john.doe@gmail.com", "Quack123#");
    }

    @Test
    void rejectsDuplicateEmailInDifferentCase() {
        when(repository.existsByEmailIgnoreCase("JOHN.DOE@gmail.com")).thenReturn(true);
        assertRejected("Email already registered", "John Doe", "JOHN.DOE@gmail.com", "Quack123#");
    }

    @ParameterizedTest
    @ValueSource(strings = {"john.duck.gmail.com", "john@gmail", "john doe@gmail.com"})
    void rejectsInvalidEmailFormat(String email) {
        assertRejected("Invalid email format", "John Duck", email, "Duck123#");
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsMissingEmail(String email) {
        assertRejected("Email is required", "John Duck", email, "Duck123#");
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quack", "quack123#", "QUACK123#", "Quackquack#", "Quack1234"})
    void rejectsWeakPassword(String password) {
        assertRejected("Password does not meet requirements", "John Dough", "john.dough@gmail.com", password);
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsMissingPassword(String password) {
        assertRejected("Password does not meet requirements", "John Dough", "john.dough@gmail.com", password);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsBlankName(String name) {
        assertRejected("Name is required", name, "john.doe@gmail.com", "Quack123#");
        verifyNoInteractions(repository);
    }
}
