package Filial;

import com.sun.net.httpserver.*;
import java.io.*;
// import java.nio.charset.StandardCharsets;

public class PaxosHandlers {

    public static class PrepareHandler implements HttpHandler {
        private final FilialState state;
        public PrepareHandler(FilialState state) { this.state = state; }

        // Request body: <proposalId>
        // Response: "OK" (promise w/o prior accepted), "ACCEPTED:<acceptedId>:<acceptedValue>", or "REJECT"
        @Override
        public void handle(HttpExchange exchange) throws IOException {            
            String proposalId = HttpUtils.readRequestBody(exchange.getRequestBody());

            PaxosResponse resp = state.handlePrepare(proposalId);
            String body;
            if (resp.status == PaxosResponse.Status.OK) {
                body = "OK";
            } else if (resp.status == PaxosResponse.Status.ACCEPTED) {
                body = "ACCEPTED:" + resp.acceptedProposalId + ":" + (resp.acceptedValue == null ? "" : resp.acceptedValue);
            } else {
                body = "REJECT";
            }
            exchange.sendResponseHeaders(200, body.getBytes().length);
            exchange.getResponseBody().write(body.getBytes());
            exchange.close();
        }
    }

    public static class AcceptHandler implements HttpHandler {
        private final FilialState state;
        public AcceptHandler(FilialState state) { this.state = state; }

        // Request body: <proposalId>|<value>
        // Response: "ACCEPTED" or "REJECT"
        @Override
        public void handle(HttpExchange exchange) throws IOException {            
            String body = HttpUtils.readRequestBody(exchange.getRequestBody());
            String[] parts = body.split("\\|", 2);
            String proposalId = parts[0];
            String value = parts.length > 1 ? parts[1] : "";
            boolean ok = state.handleAccept(proposalId, value);
            String resp = ok ? "ACCEPTED" : "REJECT";
            exchange.sendResponseHeaders(200, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());
            exchange.close();
        }
    }

    public static class CommitHandler implements HttpHandler {
        private final FilialState state;
        public CommitHandler(FilialState state) { this.state = state; }

        // Request body: <value>  (the command to apply)
        // Response: "OK" if applied
        @Override
        public void handle(HttpExchange exchange) throws IOException {            
            String value = HttpUtils.readRequestBody(exchange.getRequestBody());
            state.handleCommit(value);
            String resp = "OK";
            exchange.sendResponseHeaders(200, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());
            exchange.close();
        }
    }
}
