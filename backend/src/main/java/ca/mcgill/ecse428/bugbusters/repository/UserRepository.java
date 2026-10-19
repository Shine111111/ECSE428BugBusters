package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.User;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    // Keyed by email so that an email can only be registered once
    private final Map<String, User> users = new HashMap<>();

    public void save(User user) {
        if (users.containsKey(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        users.put(user.getEmail(), user);
    }

    public User findByEmail(String email) {
        return users.get(email);
    }
}
