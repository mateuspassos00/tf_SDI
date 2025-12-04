package Filial;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;

public class EntregaHandler implements HttpHandler {

    private FilialState state;

    public EntregaHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();
        String query = uri.getQuery(); // pedido=5
        System.out.print(query);
        int pedidoId = Integer.parseInt(query.split("=")[1]);

        int tempo = state.tempoEntrega(pedidoId);

        byte[] response = String.valueOf(tempo).getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
