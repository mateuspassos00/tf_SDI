package Filial;

import com.sun.net.httpserver.*;
import java.io.*;
import java.util.*;

public class ComprarHandler implements HttpHandler {

    private FilialState state;

    public ComprarHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!state.isLeader()) {
            exchange.sendResponseHeaders(403, 0);
            exchange.close();
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        String[] parts = body.split(";");

        int pedidoId = Integer.parseInt(parts[0]);
        List<String> produtos = Arrays.asList(parts[1].split(","));

        // ✅ HERE is where Paxos COMMIT will go
        boolean ok = state.comprar(pedidoId, produtos);

        byte[] response = String.valueOf(ok).getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
