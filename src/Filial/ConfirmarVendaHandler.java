package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfirmarVendaHandler implements HttpHandler {

    private final FilialState state;
    private final ObjectMapper mapper = new ObjectMapper();

    public ConfirmarVendaHandler(FilialState state) {
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

        ConfirmSaleRequest req =
                mapper.readValue(json.toString(), ConfirmSaleRequest.class);

        boolean success = state.removeFromStock(req.product, req.orderId);

        String response = success ? "true" : "false";
        byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(200, respBytes.length);
        exchange.getResponseBody().write(respBytes);
        exchange.close();

        System.out.println("✅ " + state.getFilialUrl() +
                " sale " + (success ? "CONFIRMED" : "FAILED") +
                " for product: " + req.product);
    }
}
