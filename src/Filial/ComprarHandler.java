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

        // inside handle method of ComprarHandler (when leader)        
        String body = HttpUtils.readRequestBody(exchange.getRequestBody());

        String[] parts = body.split(";");
        int pedidoId = Integer.parseInt(parts[0]);
        List<String> produtos = Arrays.asList(parts[1].split(","));

        String command = "COMPRAR:" + pedidoId + ":" + String.join(",", produtos);

        // run Paxos commit
        boolean paxosOk = state.proposeAndCommit(command);
        boolean ok = false;
        if (paxosOk) {
            // state.handleCommit has already applied it on the leader because the commit RPC also calls handleCommit on leader
            // but to be safe, ensure local state also applied (leader's commit handler may have applied it already)
            ok = state.tempoEntrega(pedidoId) >= 0; // or check pedido existence
        } else {
            ok = false;
        }

        byte[] response = String.valueOf(ok).getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
