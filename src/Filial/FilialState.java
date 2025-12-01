package Filial;

import java.util.*;

public class FilialState {

    private boolean isLeader = false;
    private String myUrl;
    private String currentLeader;

    private Map<Integer, Pedido> pedidos = new HashMap<>();
    private int nextPedidoId = 1;

    public FilialState(String myUrl) {
        this.myUrl = myUrl;
    }

    // ===== Leader control =====

    public synchronized boolean isLeader() {
        return isLeader;
    }

    public synchronized void setLeader(String leaderUrl) {
        this.currentLeader = leaderUrl;
        this.isLeader = leaderUrl.equals(myUrl);
        System.out.println("👑 New leader: " + leaderUrl);
    }

    public synchronized String getLeader() {
        return currentLeader;
    }

    // ===== Pedido Logic =====

    public synchronized int cadastrarPedido(String restaurante) {
        int id = nextPedidoId++;
        pedidos.put(id, new Pedido(id, restaurante));
        return id;
    }

    public synchronized boolean comprar(int pedidoId, List<String> produtos) {
        Pedido p = pedidos.get(pedidoId);
        if (p == null) return false;
        p.setProdutos(produtos);
        return true;
    }

    public synchronized int tempoEntrega(int pedidoId) {
        Pedido p = pedidos.get(pedidoId);
        if (p == null) return -1;
        return p.getTempoEntrega();
    }
}
