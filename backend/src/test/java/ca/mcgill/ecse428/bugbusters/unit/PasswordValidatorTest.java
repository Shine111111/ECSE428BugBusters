package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.mcgill.ecse428.bugbusters.service.PasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

//Unit tests for the password validator
class PasswordValidatorTest {

    private void assertRejected(String password) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> PasswordValidator.validate(password));
        assertEquals("Password does not meet requirements", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quack123#", "Dough123#", "Duck123#", "PASSWORD1"})
    void acceptValidPassword(String password) {
        assertDoesNotThrow(() -> PasswordValidator.validate(password));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectMissingPassword(String password) {
        assertRejected(password);
    }

    @Test
    void rejectFeatureFileExample() {
        assertRejected("Quack");
    }

    @Test
    void rejectPasswordOfSixCharacters() {
        assertRejected("Abcde1");
    }

    @Test
    void acceptPasswordOfSevenCharacters() {
        assertDoesNotThrow(() -> PasswordValidator.validate("Abcdef1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"quack123#", "abcdefg1"})
    void rejectPasswordWithoutUppercase(String password) {
        assertRejected(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quackquack#", "Abcdefgh"})
    void rejectPasswordWithoutDigit(String password) {
        assertRejected(password);
    }
}

