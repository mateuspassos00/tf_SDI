package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReplicarPlanoHandler implements HttpHandler {

    private final FilialState state;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReplicarPlanoHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public synchronized void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
        );

        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        FinalPlanRequest req =
                mapper.readValue(json.toString(), FinalPlanRequest.class);

        state.storeFinalPlan(req.orderId, req.plan);

        byte[] resp = "true".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();

        System.out.println("📦 Final plan replicated on " +
                state.getFilialUrl() + " for order " + req.orderId);
    }
}
