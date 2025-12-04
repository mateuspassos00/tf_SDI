package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FinalPlansHandler implements HttpHandler {

    private final FilialState state;
    private final ObjectMapper mapper = new ObjectMapper();

    public FinalPlansHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<Integer, Map<String, String>> plans = state.getFinalPlans();

        byte[] response = mapper.writeValueAsBytes(plans);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();

        System.out.println("📦 /finalPlans requested from " + state.getFilialUrl());
    }
}
