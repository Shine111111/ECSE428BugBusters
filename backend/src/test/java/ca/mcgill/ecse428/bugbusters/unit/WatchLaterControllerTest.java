package ca.mcgill.ecse428.bugbusters.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.mcgill.ecse428.bugbusters.controller.WatchLaterController;
import ca.mcgill.ecse428.bugbusters.dto.WatchLaterResponse;
import ca.mcgill.ecse428.bugbusters.service.WatchLaterService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// TID031: unit tests for the Watch Later REST controller (the service is mocked)
@WebMvcTest(WatchLaterController.class)
class WatchLaterControllerTest {

    private static final String USER_ID = "0001";
    private static final String MOVIE_ID = "tt0110912";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchLaterService watchLaterService;

    private ResultActions addMovie(String userId, String movieId) throws Exception {
        return mockMvc.perform(post("/users/{userId}/watch-later/{movieId}", userId, movieId));
    }

    private void stubRejected(String message) {
        when(watchLaterService.addMovieToWatchLater(USER_ID, MOVIE_ID))
                .thenThrow(new IllegalArgumentException(message));
    }

    // ---- POST /users/{userId}/watch-later/{movieId} ----

    @Test
    void addReturns201WithConfirmationMessage() throws Exception {
        when(watchLaterService.addMovieToWatchLater(USER_ID, MOVIE_ID))
                .thenReturn(new WatchLaterResponse(USER_ID, MOVIE_ID, WatchLaterService.MOVIE_ADDED));

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.movieId").value(MOVIE_ID))
                .andExpect(jsonPath("$.message").value(WatchLaterService.MOVIE_ADDED));
    }

    @Test
    void addFirstMovieReturns201WithFirstMovieMessage() throws Exception {
        when(watchLaterService.addMovieToWatchLater(USER_ID, MOVIE_ID))
                .thenReturn(new WatchLaterResponse(USER_ID, MOVIE_ID, WatchLaterService.FIRST_MOVIE_ADDED));

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(WatchLaterService.FIRST_MOVIE_ADDED));
    }

    @Test
    void addPassesPathVariablesToService() throws Exception {
        when(watchLaterService.addMovieToWatchLater("0042", "tt0816692"))
                .thenReturn(new WatchLaterResponse("0042", "tt0816692", WatchLaterService.MOVIE_ADDED));

        addMovie("0042", "tt0816692").andExpect(status().isCreated());

        verify(watchLaterService).addMovieToWatchLater("0042", "tt0816692");
    }

    @Test
    void duplicateMovieReturns409() throws Exception {
        stubRejected(WatchLaterService.MOVIE_ALREADY_ADDED);

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(WatchLaterService.MOVIE_ALREADY_ADDED));
    }

    @Test
    void concurrentDuplicateInsertReturns409() throws Exception {
        when(watchLaterService.addMovieToWatchLater(USER_ID, MOVIE_ID))
                .thenThrow(new DataIntegrityViolationException("uk_watch_later_user_movie"));

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(WatchLaterService.MOVIE_ALREADY_ADDED));
    }

    @Test
    void unknownMovieReturns404() throws Exception {
        stubRejected(WatchLaterService.MOVIE_NOT_EXIST);

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(WatchLaterService.MOVIE_NOT_EXIST));
    }

    @Test
    void unknownUserReturns404() throws Exception {
        stubRejected(WatchLaterService.USER_NOT_FOUND);

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(WatchLaterService.USER_NOT_FOUND));
    }

    @Test
    void notLoggedInReturns401() throws Exception {
        stubRejected(WatchLaterService.LOGIN_REQUIRED);

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(WatchLaterService.LOGIN_REQUIRED));
    }

    @Test
    void otherRejectionReturns400() throws Exception {
        stubRejected("Something else went wrong");

        addMovie(USER_ID, MOVIE_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Something else went wrong"));
    }

    @Test
    void addWithoutMovieIdIsNotRouted() throws Exception {
        mockMvc.perform(post("/users/{userId}/watch-later", USER_ID))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(watchLaterService);
    }

    // ---- GET /users/{userId}/watch-later ----

    @Test
    void listReturns200WithMovieIds() throws Exception {
        when(watchLaterService.getWatchLaterMovieIds(USER_ID))
                .thenReturn(List.of("tt0468569", "tt0816692", "tt0110912"));

        mockMvc.perform(get("/users/{userId}/watch-later", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("tt0468569"))
                .andExpect(jsonPath("$[1]").value("tt0816692"))
                .andExpect(jsonPath("$[2]").value("tt0110912"));
    }

    @Test
    void listOfEmptyPlaylistReturnsEmptyArray() throws Exception {
        when(watchLaterService.getWatchLaterMovieIds(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/users/{userId}/watch-later", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listWhenNotLoggedInReturns401() throws Exception {
        when(watchLaterService.getWatchLaterMovieIds(anyString()))
                .thenThrow(new IllegalArgumentException(WatchLaterService.LOGIN_REQUIRED));

        mockMvc.perform(get("/users/{userId}/watch-later", " "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(WatchLaterService.LOGIN_REQUIRED));
    }
}
