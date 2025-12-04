package Filial;

import com.sun.net.httpserver.*;
import java.io.IOException;

public class ElectionHandler implements HttpHandler {

    private final FilialState state;

    public ElectionHandler(FilialState state) {
        this.state = state;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 2);
        exchange.getResponseBody().write("OK".getBytes());
        exchange.close();
    }
}
