package Mercado;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
 
@WebService(endpointInterface = "Mercado.MercadoServidor")
public class MercadoServidorImpl implements MercadoServidor {
    private List<Pedido> pedidos = new ArrayList<>();
    private int idNextPedido = 1;

    @Override
    public int cadastrarPedido(String restaurante) {
        int id = idNextPedido++;
        Pedido p = new Pedido(id, restaurante);
        pedidos.add(p);

        System.out.println("Pedido cadastrado: ID " + id + " | Restaurante=" + restaurante);

        return id;
    }

    @Override
    public boolean comprarProdutos(int pedidoId, String[] produtos) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null) {
            System.out.println("Pedido " + pedidoId + " não encontrado!");
            return false;
        }

        pedido.setProdutos(Arrays.asList(produtos));

        System.out.println("Produtos adicionados ao pedido " + pedidoId + ":");
        for (String p : produtos) {
            System.out.println(" - " + p);
        }
        return true;
    }

    @Override
    public int tempoEntrega(int pedidoId) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null) {
            System.out.println("Pedido " + pedidoId + " não encontrado!");
            return -1;
        }
        return pedido.getTempoEntrega();
    }

    private Pedido buscarPedidoPorId(int id) {
        for (Pedido p : pedidos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
}