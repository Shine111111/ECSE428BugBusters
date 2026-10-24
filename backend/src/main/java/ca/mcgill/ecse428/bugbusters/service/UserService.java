package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    public static final String EMAIL_ALREADY_REGISTERED = "Email already registered";
    public static final String NAME_REQUIRED = "Name is required";

    private final UserRepository userRepository;
    private final UserIdGenerator userIdGenerator;

    public UserService(UserRepository userRepository, UserIdGenerator userIdGenerator) {
        this.userRepository = userRepository;
        this.userIdGenerator = userIdGenerator;
    }

    // TID010 - creates an account; throws IllegalArgumentException with the message to show the user
    @Transactional
    public User createUser(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(NAME_REQUIRED);
        }
        EmailValidator.validate(email);
        PasswordValidator.validate(password);
        // TID011 - duplicate email check
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(EMAIL_ALREADY_REGISTERED);
        }
        User user = new User(userIdGenerator.nextId(), name.trim(), email, password);
        return userRepository.save(user);
    }
}
