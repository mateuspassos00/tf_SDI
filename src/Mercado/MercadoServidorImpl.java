package Mercado;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
 
@WebService(endpointInterface = "Mercado.MercadoServidor")
public class MercadoServidorImpl implements MercadoServidor {

    private LeaderLocator leaderLocator = new LeaderLocator();

    @Override
    public int cadastrarPedido(String restaurante) {
        FilialClient filial = leaderLocator.getLeaderClient();
        return filial.cadastrarPedido(restaurante);
    }

    @Override
    public boolean comprarProdutos(int pedidoId, String[] produtos) {
        FilialClient filial = leaderLocator.getLeaderClient();
        return filial.comprarProdutos(pedidoId, produtos);
    }

    @Override
    public int tempoEntrega(int pedidoId) {
        FilialClient filial = leaderLocator.getLeaderClient();
        return filial.tempoEntrega(pedidoId);
    }
}
