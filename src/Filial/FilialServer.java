package Filial;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class FilialServer {
    
    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]); // Example: 9001        
        List<String> filiais = Arrays.asList(
            "http://localhost:9001",
            "http://localhost:9002",
            "http://localhost:9003",
            "http://localhost:9004",
            "http://localhost:9005"
    );
        FilialState state = new FilialState("http://localhost:" + port, filiais);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/cadastrar", new CadastrarHandler(state));
        server.createContext("/comprar", new ComprarHandler(state));
        server.createContext("/entrega", new EntregaHandler(state));
        server.createContext("/isLeader", new IsLeaderHandler(state));
        server.createContext("/setLeader", new SetLeaderHandler(state));

        
        // new Paxos endpoints
        server.createContext("/paxos/prepare", new PaxosHandlers.PrepareHandler(state));
        server.createContext("/paxos/accept", new PaxosHandlers.AcceptHandler(state));
        server.createContext("/paxos/commit", new PaxosHandlers.CommitHandler(state));

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Filial running on port " + port);
    }
}
