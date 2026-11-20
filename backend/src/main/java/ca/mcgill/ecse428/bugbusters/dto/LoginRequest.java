package ca.mcgill.ecse428.bugbusters.dto;

// Request body for POST /users/login
public record LoginRequest(String email, String password) {
}
