package ca.mcgill.ecse428.bugbusters.unit;
import ca.mcgill.ecse428.bugbusters.controller.RecommendationController;
import ca.mcgill.ecse428.bugbusters.model.*;
import ca.mcgill.ecse428.bugbusters.repository.ReviewRepository;
import ca.mcgill.ecse428.bugbusters.service.*;
import ca.mcgill.ecse428.bugbusters.service.recommendation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.junit.jupiter.api.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class RecommendationControllerTest {
    private HttpServer server;
    private String base;
    private boolean fail;
    @BeforeEach void start() throws Exception {
        var data = new InMemoryRecommendationDataProvider(new ReviewService(new ReviewRepository()));
        data.saveMovie(new Movie("m", "Arrival", Set.of("Drama"), Set.of(), "D", 4.6, 900, 200, 1));
        data.saveWatchedMovies("alice", Set.of("m"));
        var service = new RecommendationService(data, new WatchedMovieFilter(), new PreferenceScorer(), new RatingHistoryScorer(), new PopularityScorer(), new RecommendationRanker()) {
            @Override public java.util.List<ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse> getRecommendations(String user) {
                if (fail) throw new IllegalStateException("private backend details");
                return super.getRecommendations(user);
            }
        };
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        new RecommendationController(service).register(server, new BasicAuthenticator("test") {
            public boolean checkCredentials(String user, String password) { return Set.of("alice", "bob").contains(user) && password.equals("secret"); }
        });
        server.start();
        base = "http://localhost:" + server.getAddress().getPort();
    }
    @AfterEach void stop() { server.stop(0); }
    private HttpResponse<String> request(String method, String path, String user) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(base + path)).method(method, HttpRequest.BodyPublishers.noBody());
        if (user != null) builder.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":secret").getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void returnsJsonUsingAuthenticatedUserRatherThanQueryIdentity() throws Exception {
        var response = request("GET", "/recommendations?userId=alice", "bob");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElseThrow().contains("application/json"));
        var json = new ObjectMapper().readTree(response.body());
        assertEquals("m", json.get(0).get("movie").get("id").asText());
        assertEquals(20.2, json.get(0).get("totalScore").asDouble(), 0.00001);
        assertEquals("[]", request("GET", "/recommendations", "alice").body());
    }
    @Test void requiresAuthentication() throws Exception { assertEquals(401, request("GET", "/recommendations", null).statusCode()); }
    @Test void rejectsUnsupportedMethodAndUnknownPath() throws Exception {
        var response = request("POST", "/recommendations", "bob");
        assertEquals(405, response.statusCode()); assertEquals("GET", response.headers().firstValue("Allow").orElseThrow());
        assertEquals(404, request("GET", "/recommendations/unknown", "bob").statusCode());
    }
    @Test void hidesInternalErrors() throws Exception {
        fail = true; var response = request("GET", "/recommendations", "bob");
        assertEquals(500, response.statusCode()); assertFalse(response.body().contains("private backend details"));
    }
}
