package Filial;

import com.sun.net.httpserver.*;
import java.io.*;

public class IsLeaderHandler implements HttpHandler {

    private FilialState state;

    public IsLeaderHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = String.valueOf(state.isLeader());
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
