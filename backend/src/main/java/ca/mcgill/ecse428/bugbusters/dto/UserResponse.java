package ca.mcgill.ecse428.bugbusters.dto;

import ca.mcgill.ecse428.bugbusters.model.User;

// User data returned by the API; never includes the password
public record UserResponse(String userId, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail());
    }
}
