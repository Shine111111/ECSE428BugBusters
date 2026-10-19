package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.mcgill.ecse428.bugbusters.service.EmailValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

// Unit test for the EmailValidator
class EmailValidatorTest {

    private void assertInvalidFormat(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> EmailValidator.validate(email));
        assertEquals("Invalid email format", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "john.doe@gmail.com",
            "john.dough@gmail.com",
            "a@b.c",
            "user@mail.mcgill.ca",
            "first.last+tag@sub.domain.org"
    })
    void acceptValidEmail(String email) {
        assertDoesNotThrow(() -> EmailValidator.validate(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectMissingEmail(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> EmailValidator.validate(email));
        assertEquals("Email is required", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"john.duck.gmail.com", "johngmail.com"})
    void rejectEmailWithoutAt(String email) {
        assertInvalidFormat(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"john@doe@gmail.com", "john@@gmail.com"})
    void rejectEmailWithMultipleAts(String email) {
        assertInvalidFormat(email);
    }

    @Test
    void rejectEmailWithNothingBeforeAt() {
        assertInvalidFormat("@gmail.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@", "user@com", "user@gmailcom"})
    void rejectDomainWithoutDot(String email) {
        assertInvalidFormat(email);
    }

    @Test
    void rejectDotRightAfterAt() {
        assertInvalidFormat("user@.com");
    }

    @Test
    void rejectDotAtEnd() {
        assertInvalidFormat("user@com.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "john doe@gmail.com",
            "john@gm ail.com",
            " john@gmail.com",
            "john@gmail.com ",
            "john\t@gmail.com",
            "   "
    })
    void rejectEmailWithWhitespace(String email) {
        assertInvalidFormat(email);
    }

    @Test
    void acceptEmailOfExactlyMaxLength() {
        String domain = "@gmail.com";
        String email = "a".repeat(EmailValidator.MAX_LENGTH - domain.length()) + domain;
        assertEquals(254, email.length());
        assertDoesNotThrow(() -> EmailValidator.validate(email));
    }

    @Test
    void rejectEmailLongerThanMaxLength() {
        String domain = "@gmail.com";
        String email = "a".repeat(EmailValidator.MAX_LENGTH - domain.length() + 1) + domain;
        assertEquals(255, email.length());
        assertInvalidFormat(email);
    }
}
