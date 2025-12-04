package Filial;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class FilialServer {
    
    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]); // Example: 9001
        FilialState state = new FilialState(
            "http://localhost:" + port,
            "filial" + port + ".csv"
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/cadastrar", new CadastrarHandler(state));
        server.createContext("/comprar", new ComprarHandler(state));
        server.createContext("/entrega", new EntregaHandler(state));
        server.createContext("/isLeader", new IsLeaderHandler(state));
        server.createContext("/setLeader", new SetLeaderHandler(state));
        server.createContext("/ofertar", new OfertarHandler(state));
        server.createContext("/confirmarVenda", new ConfirmarVendaHandler(state));
        server.createContext("/cancelarReserva", new CancelarReservaHandler(state));


        server.setExecutor(null);
        server.start();

        System.out.println("✅ Filial running on port " + port);
    }
}
