package ca.mcgill.ecse428.bugbusters.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.bugbusters.validation.EmailValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

//Unit tests for the email validator
class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "john.doe@gmail.com",
            "john.dough@gmail.com",
            "john.duck@gmail.com",
            "JOHN.DOE@GMAIL.COM",
            "john_doe+movies@mail.mcgill.ca",
            "j@sub.domain.org",
            "john-doe123@my-domain.co"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @Test
    void rejectsEmailWithoutAtSign() {
        assertFalse(EmailValidator.isValid("john.duck.gmail.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "@gmail.com",
            "john.doe@",
            "john.doe@gmail",
            "john.doe@gmail.",
            "john.doe@.com",
            "john..doe@gmail.com",
            ".john@gmail.com",
            "john.@gmail.com",
            "john@doe@gmail.com",
            "john doe@gmail.com",
            "john.doe@gmail.c",
            "john.doe@-gmail.com",
            "john.doe@gmail-.com",
            " john.doe@gmail.com",
            "john.doe@gmail.com "
    })
    void rejectsMalformedEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNullEmail() {
        assertFalse(EmailValidator.isValid(null));
    }
}
