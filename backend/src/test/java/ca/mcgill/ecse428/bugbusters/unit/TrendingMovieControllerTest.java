package ca.mcgill.ecse428.bugbusters.unit;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.mcgill.ecse428.bugbusters.controller.TrendingMovieController;
import ca.mcgill.ecse428.bugbusters.dto.TrendingMovieResponse;
import ca.mcgill.ecse428.bugbusters.dto.TrendingMoviesResponse;
import ca.mcgill.ecse428.bugbusters.service.TrendingMovieService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrendingMovieController.class)
class TrendingMovieControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrendingMovieService trendingMovieService;

    @Test
    void returnsTrendingMovieSignalsAndScoreAsJson() throws Exception {
        var movie = new TrendingMovieResponse("tt0468569", "The Dark Knight",
                new BigDecimal("9.0"), 700_000L, 5_000, 900_000, new BigDecimal("1110.0"));
        when(trendingMovieService.getTrendingMovies())
                .thenReturn(new TrendingMoviesResponse(List.of(movie), null));

        mockMvc.perform(get("/movies/trending"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.movies.length()").value(1))
                .andExpect(jsonPath("$.movies[0].movieId").value("tt0468569"))
                .andExpect(jsonPath("$.movies[0].title").value("The Dark Knight"))
                .andExpect(jsonPath("$.movies[0].rating").value(9.0))
                .andExpect(jsonPath("$.movies[0].numberOfVotes").value(700_000))
                .andExpect(jsonPath("$.movies[0].commentCount").value(5_000))
                .andExpect(jsonPath("$.movies[0].viewCount").value(900_000))
                .andExpect(jsonPath("$.movies[0].trendingScore").value(1110.0))
                .andExpect(jsonPath("$.message").value(nullValue()));

        verify(trendingMovieService).getTrendingMovies();
    }

    @Test
    void emptyTrendingResultReturnsSuccessWithInformativeMessage() throws Exception {
        when(trendingMovieService.getTrendingMovies())
                .thenReturn(new TrendingMoviesResponse(List.of(), "No trending movies available"));

        mockMvc.perform(get("/movies/trending"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.movies").isArray())
                .andExpect(jsonPath("$.movies").isEmpty())
                .andExpect(jsonPath("$.message").value("No trending movies available"));

        verify(trendingMovieService).getTrendingMovies();
    }
}
