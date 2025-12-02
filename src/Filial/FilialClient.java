package Filial;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class FilialClient {

    private final String baseUrl; // Example: http://localhost:9001

    public FilialClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // ============================
    // POST /cadastrar
    // ============================
    public int cadastrarPedido(String restaurante) {
        try {
            URL url = new URL(baseUrl + "/cadastrar");
            HttpURLConnection conn = createPostConnection(url, restaurante);

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Erro ao cadastrar pedido. HTTP " + status);
            }

            String response = readResponse(conn);
            return Integer.parseInt(response);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar /cadastrar", e);
        }
    }

    // ============================
    // POST /comprar
    // Body format: pedidoId;produto1,produto2,produto3
    // ============================
    public boolean comprarProdutos(int pedidoId, String[] produtos) {
        try {
            String body = pedidoId + ";" + String.join(",", produtos);

            URL url = new URL(baseUrl + "/comprar");
            HttpURLConnection conn = createPostConnection(url, body);

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Erro ao comprar produtos. HTTP " + status);
            }

            String response = readResponse(conn);
            return Boolean.parseBoolean(response);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar /comprar", e);
        }
    }

    // ============================
    // GET /entrega?pedido=ID
    // ============================
    public int tempoEntrega(int pedidoId) {
        try {
            URL url = new URL(baseUrl + "/entrega?pedido=" + pedidoId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Erro ao consultar entrega. HTTP " + status);
            }

            String response = readResponse(conn);
            return Integer.parseInt(response);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar /entrega", e);
        }
    }

    // ============================
    // GET /isLeader
    // ============================
    public boolean isLeader() {
        try {
            URL url = new URL(baseUrl + "/isLeader");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Erro ao consultar leader. HTTP " + status);
            }

            String response = readResponse(conn);
            return Boolean.parseBoolean(response);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar /isLeader", e);
        }
    }

    // =====================================================
    // =============== Helper methods ======================
    // =====================================================

    private HttpURLConnection createPostConnection(URL url, String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
        }

        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
