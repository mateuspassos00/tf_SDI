package Filial;

import com.sun.net.httpserver.*;
import java.io.*;

public class SetLeaderHandler implements HttpHandler {

    private FilialState state;

    public SetLeaderHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String leaderUrl = new String(exchange.getRequestBody().readAllBytes());
        state.setLeader(leaderUrl);
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
