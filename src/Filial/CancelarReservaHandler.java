package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CancelarReservaHandler implements HttpHandler {

    private final FilialState state;
    private final ObjectMapper mapper = new ObjectMapper();

    public CancelarReservaHandler(FilialState state) {
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

        CancelReservationRequest req =
                mapper.readValue(json.toString(), CancelReservationRequest.class);

        state.returnToStock(req.product, req.orderId);

        exchange.sendResponseHeaders(200, -1);
        exchange.close();

        System.out.println("↩️ Reservation canceled for product: " + req.product);
    }
}
