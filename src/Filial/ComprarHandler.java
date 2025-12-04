package Filial;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ComprarHandler implements HttpHandler {

    private final LeaderPurchaseCoordinator coordinator = new LeaderPurchaseCoordinator();
    private final ObjectMapper mapper = new ObjectMapper();

    public ComprarHandler() {}

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // --- Read JSON body ---
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
        );

        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        ComprarRequest request =
                mapper.readValue(json.toString(), ComprarRequest.class);

        System.out.println("🛒 [Leader] New purchase request: " + request.products);

        // --- Call the GOOD coordinator ---
        Map<String, FilialInfo> plan =
                coordinator.purchase(request.orderId, request.products);

        // --- If failed ---
        if (plan == null) {
            String failMsg = "Purchase failed – insufficient stock or confirmation failure";
            byte[] resp = failMsg.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(409, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();

            System.out.println("❌ [Leader] Purchase FAILED for order " + request.orderId);
            return;
        }

        // --- If success ---
        PurchasePlanResponse response = new PurchasePlanResponse();
        response.orderId = request.orderId;
        response.plan = new HashMap<>();

        for (Map.Entry<String, FilialInfo> e : plan.entrySet()) {
            response.plan.put(e.getKey(), e.getValue().id);
        }

        byte[] respBytes = mapper.writeValueAsBytes(response);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, respBytes.length);
        exchange.getResponseBody().write(respBytes);
        exchange.close();

        System.out.println("✅ [Leader] Purchase SUCCESS for order " +
                request.orderId + " plan=" + response.plan);
    }
}


// package Filial;

// import com.sun.net.httpserver.HttpExchange;
// import com.sun.net.httpserver.HttpHandler;
// import java.io.*;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import java.nio.charset.StandardCharsets;

// public class ComprarHandler implements HttpHandler {

//     private final FilialState state;

//     public ComprarHandler(FilialState state) {
//         this.state = state;
//     }

//     @Override
//     public void handle(HttpExchange exchange) throws IOException {

//         if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
//             exchange.sendResponseHeaders(405, -1);
//             return;
//         }

//         // =============================
//         // ✅ Read request body (Java 8)
//         // =============================
//         BufferedReader br = new BufferedReader(
//                 new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
//         );

//         String body = br.readLine();  
//         // Format: pedidoId;produto1,produto2,...

//         String[] parts = body.split(";");
//         int pedidoId = Integer.parseInt(parts[0]);
//         String[] produtos = parts[1].split(",");

//         boolean sucesso;

//         sucesso = state.processarCompraComoLider(pedidoId, produtos);

//         byte[] response = String.valueOf(sucesso).getBytes(StandardCharsets.UTF_8);

//         exchange.sendResponseHeaders(200, response.length);
//         exchange.getResponseBody().write(response);
//         exchange.getResponseBody().close();
//     }
// }
