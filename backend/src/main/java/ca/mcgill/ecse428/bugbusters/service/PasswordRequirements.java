package ca.mcgill.ecse428.bugbusters.service;

//Password rules for account creation (ID001):
//  - at least MIN_LENGTH and at most MAX_LENGTH characters
//  - at least one uppercase letter, one lowercase letter and one digit
//  - at least one special character from SPECIAL_CHARACTERS
//  - no whitespace
public final class PasswordRequirements {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{};:'\",.<>/?\\|`~";
    public static final String ERROR_MESSAGE = "Password does not meet requirements";

    private PasswordRequirements() {
    }
}
