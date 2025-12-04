package Filial;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class FilialServer {
    
    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]); // Example: 9001
        FilialState state = new FilialState(
            "http://localhost:" + port,
            "filial" + port + ".csv",
            port
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/cadastrar", new CadastrarHandler(state)); // ok
        server.createContext("/comprar", new ComprarHandler()); // ok
        server.createContext("/entrega", new EntregaHandler(state)); // ok
        server.createContext("/isLeader", new IsLeaderHandler(state)); // ok
        server.createContext("/setLeader", new SetLeaderHandler(state));
        server.createContext("/ofertar", new OfertarHandler(state)); // ok
        server.createContext("/confirmarVenda", new ConfirmarVendaHandler(state)); // ok
        server.createContext("/cancelarReserva", new CancelarReservaHandler(state)); // ok
        server.createContext("/replicarPlano", new ReplicarPlanoHandler(state)); // ok
        server.createContext("/election", new ElectionHandler(state)); // ok

        server.setExecutor(null);
        server.start();
        new LeaderMonitor(state).start();

        System.out.println("✅ Filial running on port " + port);
    }
}
