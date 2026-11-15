package ca.mcgill.ecse428.bugbusters.controller;

import ca.mcgill.ecse428.bugbusters.dto.TrendingMoviesResponse;
import ca.mcgill.ecse428.bugbusters.service.TrendingMovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies/trending")
public class TrendingMovieController {
    private final TrendingMovieService trendingMovieService;

    public TrendingMovieController(TrendingMovieService trendingMovieService) {
        this.trendingMovieService = trendingMovieService;
    }

    @GetMapping
    public TrendingMoviesResponse getTrendingMovies() {
        return trendingMovieService.getTrendingMovies();
    }
}
