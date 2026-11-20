package ca.mcgill.ecse428.bugbusters.controller;

import ca.mcgill.ecse428.bugbusters.dto.CreateUserRequest;
import ca.mcgill.ecse428.bugbusters.dto.ErrorResponse;
import ca.mcgill.ecse428.bugbusters.dto.LoginRequest;
import ca.mcgill.ecse428.bugbusters.dto.UserResponse;
import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // TID013 - create-user endpoint: 201 with the new user, 409 for a duplicate email, 400 for invalid input
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.name(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    // TID020 - login endpoint: 200 with the user, 401 for a wrong email or password, 400 for invalid input
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.email(), request.password());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(IllegalArgumentException e) {
        HttpStatus status;
        if (UserService.EMAIL_ALREADY_REGISTERED.equals(e.getMessage())) {
            status = HttpStatus.CONFLICT;
        } else if (UserService.INVALID_CREDENTIALS.equals(e.getMessage())) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
    }

    // Two requests with the same email at the same time can both pass the duplicate check;
    // the unique constraint on users.email then rejects the second one
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateInsert(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(UserService.EMAIL_ALREADY_REGISTERED));
    }
}
