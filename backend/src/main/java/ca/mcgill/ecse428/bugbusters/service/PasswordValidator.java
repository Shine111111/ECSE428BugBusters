package ca.mcgill.ecse428.bugbusters.service;

public final class PasswordValidator {

    private PasswordValidator() {
    }

    // throws "Password does not meet requirements" if the password breaks any rule in PasswordRequirements
    public static void validate(String password) {
        if (!meetsRequirements(password)) {
            throw new IllegalArgumentException(PasswordRequirements.ERROR_MESSAGE);
        }
    }

    private static boolean meetsRequirements(String password) {
        if (password == null) {
            return false;
        }
        // checks length
        int length = password.length();
        if (length < PasswordRequirements.MIN_LENGTH || length > PasswordRequirements.MAX_LENGTH) {
            return false;
        }
        // checks character classes, rejecting any whitespace
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return false;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (PasswordRequirements.SPECIAL_CHARACTERS.indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
