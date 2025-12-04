package Filial;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FilialState {

    // Filial identity
    private final String filialUrl;
    private volatile boolean isLeader = false;
    private String leaderUrl = null;
    private final int myPort;

    // Product -> Quantity
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    // orderId -> list of reserved products
    private final Map<Integer, List<String>> reservations = new ConcurrentHashMap<>();    
    private Map<Integer, Pedido> pedidos = new HashMap<>();
    private AtomicInteger nextPedidoId = new AtomicInteger(1);    
    
    // orderId -> (product -> filialURL)
    private final Map<Integer, Map<String, String>> finalPlans = new HashMap<>();

    // ==============================
    // ✅ CONSTRUCTOR
    // ==============================
    public FilialState(String filialUrl, String csvPath, int myPort) throws IOException {
        this.filialUrl = filialUrl;
        loadStockFromCSV(csvPath);
        this.myPort = myPort;
        if(myPort == 9005) {
            this.isLeader = true;
            this.leaderUrl = filialUrl;
        }
    }

    // ==============================
    // ✅ LOAD STOCK FROM CSV
    // Format:
    // Rice,10
    // Beans,5
    // ==============================
    private void loadStockFromCSV(String csvPath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvPath));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String product = parts[0].trim();
            int qty = Integer.parseInt(parts[1].trim());
            stock.put(product, qty);
        }

        br.close();

        System.out.println("📦 Stock loaded for " + filialUrl + ": " + stock);
    }

    public synchronized int cadastrarPedido(String restaurante) {
        int id = nextPedidoId.getAndIncrement();
        pedidos.put(id, new Pedido(id, restaurante));
        System.out.println("Pedido created id=" + id + " by " + filialUrl);
        return id;
    }

    // ============================================================
    // ✅ REQUIRED METHODS USED BY YOUR HTTP HANDLERS
    // ============================================================

    // 🔹 1. Used by /ofertar
    public synchronized boolean hasStock(String product) {
        return stock.getOrDefault(product, 0) > 0;
    }

    // 🔹 2. Used by /confirmarVenda
    public synchronized boolean removeFromStock(String product, int orderId) {

        int qty = stock.getOrDefault(product, 0);

        if (qty <= 0) {
            return false;
        }

        // Decrement stock
        stock.put(product, qty - 1);

        // Track reservation for possible rollback
        reservations
            .computeIfAbsent(orderId, k -> new ArrayList<>())
            .add(product);

        return true;
    }

    // 🔹 3. Used by /cancelarReserva
    public synchronized void returnToStock(String product, int orderId) {

        List<String> products = reservations.get(orderId);

        if (products != null && products.remove(product)) {
            stock.put(product, stock.getOrDefault(product, 0) + 1);

            if (products.isEmpty()) {
                reservations.remove(orderId);
            }
        }
    }

    // 🔹 4. Used by /ofertar response
    public String getFilialUrl() {
        return filialUrl;
    }

    // ===== leader control =====
    public synchronized boolean isLeader() { return isLeader; }
    public synchronized void setLeader(String leaderUrl) {
        this.leaderUrl = leaderUrl;
        this.isLeader = leaderUrl != null && leaderUrl.equals(filialUrl);
        System.out.println("👑 leader set to " + leaderUrl + " (this=" + filialUrl + ")");
    }

    public synchronized int tempoEntrega(int pedidoId) {
        Pedido p = pedidos.get(pedidoId);
        if (p == null) return -1;
        return p.getTempoEntrega();
    }

    public synchronized void storeFinalPlan(int orderId, Map<String, String> plan) {
        finalPlans.put(orderId, plan);
    }

    public synchronized Map<String, String> getFinalPlan(int orderId) {
        return finalPlans.get(orderId);
    }

    public synchronized String getLeader() { return leaderUrl; }

    public synchronized String getLeaderUrl() { return leaderUrl; }

    // ============================================================
    // ✅ OPTIONAL DEBUG METHOD
    // ============================================================
    public synchronized Map<String, Integer> getStockSnapshot() {
        return new HashMap<>(stock);
    }

    public int getMyPort() {
        return myPort;
    }

}