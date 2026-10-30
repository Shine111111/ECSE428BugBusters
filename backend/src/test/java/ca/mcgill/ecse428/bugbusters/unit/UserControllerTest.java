package ca.mcgill.ecse428.bugbusters.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.mcgill.ecse428.bugbusters.controller.UserController;
import ca.mcgill.ecse428.bugbusters.model.User;
import ca.mcgill.ecse428.bugbusters.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

//Unit tests for the create-user endpoint (the service is mocked)
@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String JOHN_DOE =
            "{\"name\":\"John Doe\",\"email\":\"john.doe@gmail.com\",\"password\":\"Quack123#\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ResultActions postUser(String json) throws Exception {
        return mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void createUserReturns201WithTheNewUser() throws Exception {
        when(userService.createUser("John Doe", "john.doe@gmail.com", "Quack123#"))
                .thenReturn(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));

        postUser(JOHN_DOE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("0001"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@gmail.com"));
    }

    @Test
    void responseNeverContainsThePassword() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(new User("0001", "John Doe", "john.doe@gmail.com", "Quack123#"));

        postUser(JOHN_DOE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException(UserService.EMAIL_ALREADY_REGISTERED));

        postUser(JOHN_DOE)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void uniqueConstraintViolationReturns409() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("uk_users_email"));

        postUser(JOHN_DOE)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void invalidEmailReturns400() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid email format"));

        postUser("{\"name\":\"John Duck\",\"email\":\"john.duck.gmail.com\",\"password\":\"Duck123#\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format"));
    }

    @Test
    void weakPasswordReturns400() throws Exception {
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Password does not meet requirements"));

        postUser("{\"name\":\"John Dough\",\"email\":\"john.dough@gmail.com\",\"password\":\"Quack\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password does not meet requirements"));
    }

    @Test
    void missingNameReturns400() throws Exception {
        when(userService.createUser(null, "john.doe@gmail.com", "Quack123#"))
                .thenThrow(new IllegalArgumentException(UserService.NAME_REQUIRED));

        postUser("{\"email\":\"john.doe@gmail.com\",\"password\":\"Quack123#\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Name is required"));
    }

    @Test
    void malformedJsonReturns400WithoutCallingTheService() throws Exception {
        postUser("{not json")
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void getIsNotAllowed() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isMethodNotAllowed());
    }
}
