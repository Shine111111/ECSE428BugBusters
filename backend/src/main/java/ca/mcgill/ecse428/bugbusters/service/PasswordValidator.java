package ca.mcgill.ecse428.bugbusters.service;

public final class PasswordValidator {
    public static final int MIN_LENGTH = 7;

    private PasswordValidator() {
    }

    public static void validate(String password) {
        if (password == null
                || password.length() < MIN_LENGTH // Check if too short
                || password.chars().noneMatch(Character::isUpperCase) // Check if no uppercase letter
                || password.chars().noneMatch(Character::isDigit)) { // Check if no digit
            throw new IllegalArgumentException("Password does not meet requirements");
        }
    }
}
