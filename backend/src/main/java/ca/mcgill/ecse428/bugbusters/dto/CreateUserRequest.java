package ca.mcgill.ecse428.bugbusters.dto;

// Request body for POST /users
public record CreateUserRequest(String name, String email, String password) {
}
