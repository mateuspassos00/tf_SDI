package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OfertarHandler implements HttpHandler {

    private final FilialState state;
    private final ObjectMapper mapper = new ObjectMapper();

    public OfertarHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // Read request body safely (Java 8)
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
        );
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        OfferRequest request = mapper.readValue(json.toString(), OfferRequest.class);

        List<String> available = new ArrayList<>();
        for (String p : request.requestedProducts) {
            if (state.hasStock(p)) {
                available.add(p);
            }
        }

        OfferResponse response = new OfferResponse();
        response.orderId = request.orderId;
        response.filialId = state.getFilialUrl();
        response.availableProducts = available;

        byte[] respBytes = mapper.writeValueAsBytes(response);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, respBytes.length);
        exchange.getResponseBody().write(respBytes);
        exchange.close();

        System.out.println("✅ " + state.getFilialUrl() + " offered: " + available);
    }
}
