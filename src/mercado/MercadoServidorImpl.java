package mercado;

import javax.jws.WebService;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@WebService(endpointInterface = "mercado.MercadoServidor")
public class MercadoServidorImpl implements MercadoServidor {
    private Map<Integer, String> pedidos = new HashMap<>(); // id do pedido e do restaurante cliente
    private int idNextPedido = 1;

    @Override
    public int cadastrarPedido(String restaurante) {
        int id = idNextPedido++;
        pedidos.put(id, restaurante);
        System.out.println("Pedido cadastrado: ID " + id + "\nRestaurante=" + restaurante);
        return id;
    }

    @Override
    public boolean comprarProdutos(int pedidoId, String[] produtos) {
        if (!pedidos.containsKey(pedidoId)) {
            System.out.println("Pedido " + pedidoId + " não encontrado!");
            return false;
        }
        System.out.println("Produtos comprados para pedido " + pedidoId + ":");
        for (String p : produtos) {
            System.out.println(" - " + p);
        }
        return true;
    }

    @Override
    public int tempoEntrega(int pedidoId) {
        Random rand = new Random();
        try {
            if (!pedidos.containsKey(pedidoId)) {
                throw new Exception("Pedido não encontrado");
            }
            return rand.nextInt(60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
