package ca.mcgill.ecse428.bugbusters.dto;

// Body of an error response, e.g. {"error": "Invalid email format"}
public record ErrorResponse(String error) {
}
