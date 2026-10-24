package ca.mcgill.ecse428.bugbusters.service;

import ca.mcgill.ecse428.bugbusters.repository.UserRepository;
import org.springframework.stereotype.Component;

// TID009 - generates sequential, zero-padded user IDs: 0001, 0002, 0003, ...
@Component
public class UserIdGenerator {
    private final UserRepository userRepository;

    public UserIdGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String nextId() {
        Integer max = userRepository.findMaxNumericUserId();
        return String.format("%04d", max == null ? 1 : max + 1);
    }
}
