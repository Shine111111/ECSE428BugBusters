package ca.mcgill.ecse428.bugbusters.controller;
import ca.mcgill.ecse428.bugbusters.dto.MovieRecommendationResponse;
import ca.mcgill.ecse428.bugbusters.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.util.List;

/** Register with an application authenticator; user identity comes only from the authenticated principal. */
public class RecommendationController implements HttpHandler {
    private final RecommendationService recommendationService;
    private final ObjectMapper mapper = new ObjectMapper();
    public RecommendationController(RecommendationService service) { this.recommendationService = service; }
    public HttpContext register(HttpServer server, Authenticator authenticator) {
        if (authenticator == null) throw new IllegalArgumentException("Authenticator is required");
        HttpContext context = server.createContext("/recommendations", this);
        context.setAuthenticator(authenticator);
        return context;
    }
    public List<MovieRecommendationResponse> getRecommendations(String userId) {
        return recommendationService.getRecommendations(userId);
    }
    @Override public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestURI().getPath().equals("/recommendations")) { respond(exchange, 404, "Endpoint not found"); return; }
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.getResponseHeaders().set("Allow", "GET");
                respond(exchange, 405, "Method not allowed"); return;
            }
            if (exchange.getPrincipal() == null) { respond(exchange, 401, "Authentication required"); return; }
            respond(exchange, 200, getRecommendations(exchange.getPrincipal().getUsername()));
        } catch (RuntimeException e) {
            respond(exchange, 500, "Unable to retrieve recommendations");
        } finally { exchange.close(); }
    }
    private void respond(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
