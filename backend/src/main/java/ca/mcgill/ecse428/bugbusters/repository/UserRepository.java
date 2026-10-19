package ca.mcgill.ecse428.bugbusters.repository;

import ca.mcgill.ecse428.bugbusters.model.User;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//In-memory user store; user_id is the primary key and email is unique (case-insensitive)
public class UserRepository {
    private final Map<String, User> usersById = new HashMap<>();
    private final Map<String, String> userIdsByEmail = new HashMap<>();

    private String emailKey(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getUserId() == null || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID cannot be blank");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        String key = emailKey(user.getEmail());
        String ownerId = userIdsByEmail.get(key);
        if (ownerId != null && !ownerId.equals(user.getUserId())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User previous = usersById.get(user.getUserId());
        if (previous != null) {
            userIdsByEmail.remove(emailKey(previous.getEmail()));
        }
        usersById.put(user.getUserId(), user);
        userIdsByEmail.put(key, user.getUserId());
    }

    public User findById(String userId) {
        return usersById.get(userId);
    }

    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        String userId = userIdsByEmail.get(emailKey(email));
        return userId == null ? null : usersById.get(userId);
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    public int count() {
        return usersById.size();
    }
}
