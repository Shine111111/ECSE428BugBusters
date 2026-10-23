package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.mcgill.ecse428.bugbusters.service.PasswordRequirements;
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
    @ValueSource(strings = {"Quack123#", "Dough123#", "Duck123#", "Abcdef1!", "P@ssw0rd-Long_Enough"})
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
    void acceptPasswordOfMinimumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MIN_LENGTH - 4);
        assertDoesNotThrow(() -> PasswordValidator.validate(password));
    }

    @Test
    void rejectPasswordOneBelowMinimumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MIN_LENGTH - 5);
        assertRejected(password);
    }

    @Test
    void acceptPasswordOfMaximumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MAX_LENGTH - 4);
        assertDoesNotThrow(() -> PasswordValidator.validate(password));
    }

    @Test
    void rejectPasswordOneAboveMaximumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MAX_LENGTH - 3);
        assertRejected(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"quack123#", "abcdefg1!"})
    void rejectPasswordWithoutUppercase(String password) {
        assertRejected(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"QUACK123#", "PASSWORD1!"})
    void rejectPasswordWithoutLowercase(String password) {
        assertRejected(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quackquack#", "Abcdefgh!"})
    void rejectPasswordWithoutDigit(String password) {
        assertRejected(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quack1234", "Abcdefg1"})
    void rejectPasswordWithoutSpecialCharacter(String password) {
        assertRejected(password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quack 123#", "Quack123#\t", "\nQuack123#"})
    void rejectPasswordContainingWhitespace(String password) {
        assertRejected(password);
    }
}
