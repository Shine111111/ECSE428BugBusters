package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.validation.PasswordRequirements;
import ca.mcgill.ecse428.bugbusters.validation.PasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

//Unit tests for the password validator
class PasswordValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"Quack123#", "Dough123#", "Duck123#", "Abcdef1!", "P@ssw0rd-Long_Enough"})
    void acceptsPasswordsMeetingAllRequirements(String password) {
        assertTrue(PasswordValidator.isValid(password));
    }

    @Test
    void rejectsShortPasswordFromFeatureFile() {
        assertFalse(PasswordValidator.isValid("Quack"));
    }

    @Test
    void acceptsPasswordAtMinimumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MIN_LENGTH - 4);
        assertTrue(PasswordValidator.isValid(password));
    }

    @Test
    void rejectsPasswordOneBelowMinimumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MIN_LENGTH - 5);
        assertFalse(PasswordValidator.isValid(password));
    }

    @Test
    void acceptsPasswordAtMaximumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MAX_LENGTH - 4);
        assertTrue(PasswordValidator.isValid(password));
    }

    @Test
    void rejectsPasswordOneAboveMaximumLength() {
        String password = "Aa1#" + "a".repeat(PasswordRequirements.MAX_LENGTH - 3);
        assertFalse(PasswordValidator.isValid(password));
    }

    @Test
    void rejectsPasswordWithoutUppercase() {
        assertFalse(PasswordValidator.isValid("quack123#"));
    }

    @Test
    void rejectsPasswordWithoutLowercase() {
        assertFalse(PasswordValidator.isValid("QUACK123#"));
    }

    @Test
    void rejectsPasswordWithoutDigit() {
        assertFalse(PasswordValidator.isValid("Quackers#"));
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {
        assertFalse(PasswordValidator.isValid("Quack1234"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Quack 123#", "Quack123#\t", "\nQuack123#"})
    void rejectsPasswordContainingWhitespace(String password) {
        assertFalse(PasswordValidator.isValid(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "        "})
    void rejectsBlankPasswords(String password) {
        assertFalse(PasswordValidator.isValid(password));
    }

    @Test
    void rejectsNullPassword() {
        assertFalse(PasswordValidator.isValid(null));
    }
}
