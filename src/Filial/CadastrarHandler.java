package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class CadastrarHandler implements HttpHandler {

    private final FilialState state;

    public CadastrarHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // ✅ Read body safely in Java 8
        BufferedReader br = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
        );

        String restaurante = br.readLine();  // just one line

        int pedidoId = state.cadastrarPedido(restaurante);

        byte[] response = String.valueOf(pedidoId).getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
}
