package ca.mcgill.ecse428.bugbusters.service;

public final class EmailValidator {
    public static final int MAX_LENGTH = 254;

    private EmailValidator() {
    }

    // throws an error if the email is null ("Email is required") and if the email is not well-formed ("Invalid email format")
    public static void validate(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isWellFormed(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static boolean isWellFormed(String email) {
        // checks length
        if (email.length() > MAX_LENGTH) {
            return false;
        }
        //checks for whitespace
        for (char c : email.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return false;
            }
        }
        // checks for exactly one '@' character   
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }
        // checks the part before '@' does not start or end with a dot or contain consecutive dots
        String local = email.substring(0, at);
        if (local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            return false;
        }
        // checks for a valid domain (contains at least one dot, does not start or end with a dot)
        String domain = email.substring(at + 1);
        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        // checks each domain label is non-empty and does not start or end with a hyphen
        for (String label : domain.split("\\.", -1)) {
            if (label.isEmpty() || label.startsWith("-") || label.endsWith("-")) {
                return false;
            }
        }
        return true;
    }
}
