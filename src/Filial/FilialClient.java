package Filial;

import com.sun.net.httpserver.HttpServer;

public class FilialClient {
    
    private final String baseUrl;

    public FilialClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int cadastrarPedido(String restaurante) {
        return Http.post(baseUrl + "/cadastrar", restaurante, int.class);
    }

    public boolean comprarProdutos(int pedidoId, String[] produtos) {
        return Http.post(baseUrl + "/comprar", new CompraRequest(pedidoId, produtos), boolean.class);
    }

    public int tempoEntrega(int pedidoId) {
        return Http.get(baseUrl + "/entrega?pedido=" + pedidoId, int.class);
    }
}
