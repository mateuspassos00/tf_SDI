package Filial;

import com.sun.net.httpserver.*;
import java.io.*;

public class CadastrarHandler implements HttpHandler {

    private FilialState state;

    public CadastrarHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!state.isLeader()) {
            exchange.sendResponseHeaders(403, 0);
            exchange.close();
            return;
        }
        
        String restaurante = HttpUtils.readRequestBody(exchange.getRequestBody());


        // ✅ HERE is where Paxos will be called in the future
        int pedidoId = state.cadastrarPedido(restaurante);

        byte[] response = String.valueOf(pedidoId).getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
