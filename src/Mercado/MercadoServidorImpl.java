package Mercado;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import Filial.FilialClient;
import Filial.FilialInfo;
import Filial.LeaderLocator;
import Filial.LeaderPurchaseCoordinator;
 
@WebService(endpointInterface = "Mercado.MercadoServidor")
public class MercadoServidorImpl implements MercadoServidor {

    private LeaderLocator leaderLocator = new LeaderLocator();

    @Override
    public int cadastrarPedido(String restaurante) {
        return withFailover(client -> client.cadastrarPedido(restaurante));
    }

    // @Override
    // public boolean comprarProdutos(int pedidoId, String[] produtos) {
    //     return withFailover(client -> client.comprarProdutos(pedidoId, produtos));
    // }

    @Override
    public boolean comprarProdutos(int pedidoId, String[] produtos) {

        LeaderPurchaseCoordinator coordinator = new LeaderPurchaseCoordinator();

        Map<String, FilialInfo> plan =
                coordinator.purchase(pedidoId, Arrays.asList(produtos));

        return plan != null;
    }


    @Override
    public int tempoEntrega(int pedidoId) {
        return withFailover(client -> client.tempoEntrega(pedidoId));
    }

    // ✅ Generic Failover Wrapper
    private <T> T withFailover(FilialOperation<T> op) {
        try {
            FilialClient leader = leaderLocator.getLeaderClient();
            return op.execute(leader);
        } catch (Exception e) {
            System.out.println("⚠️ Leader failed, retrying with new leader...");
            leaderLocator.invalidateLeader();

            FilialClient newLeader = leaderLocator.getLeaderClient();
            return op.execute(newLeader);
        }
    }

    // ✅ Functional interface for retry logic
    private interface FilialOperation<T> {
        T execute(FilialClient client);
    }
}

