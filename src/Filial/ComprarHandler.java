package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ComprarHandler implements HttpHandler {

    private final FilialState state;

    public ComprarHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // =============================
        // ✅ Read request body (Java 8)
        // =============================
        BufferedReader br = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
        );

        String body = br.readLine();  
        // Format: pedidoId;produto1,produto2,...

        String[] parts = body.split(";");
        int pedidoId = Integer.parseInt(parts[0]);
        String[] produtos = parts[1].split(",");

        boolean sucesso;

        // =========================================
        // ✅ If NOT leader → redirect to leader
        // =========================================
        if (!state.isLeader()) {

            String leaderUrl = state.getLeader();

            URL url = new URL(leaderUrl + "/comprar");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(bodyBytes);

            BufferedReader leaderResp = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            sucesso = Boolean.parseBoolean(leaderResp.readLine());

        } else {

            // =====================================================
            // ✅ LEADER LOGIC (calls /ofertar, builds plan, etc.)
            // =====================================================
            sucesso = state.processarCompraComoLider(pedidoId, produtos);
        }

        byte[] response = String.valueOf(sucesso).getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
}
