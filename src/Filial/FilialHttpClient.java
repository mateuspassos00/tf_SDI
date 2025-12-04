package Filial;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class FilialHttpClient {

    private static final int CONNECT_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 4000;
    private final ObjectMapper mapper = new ObjectMapper();
    private final FilialInfo filial;

    public FilialHttpClient(FilialInfo filial) {
        this.filial = filial;
    }

    // POST /ofertar  -> OfferResponse (200)
    public OfferResponse callOfertar(OfferRequest req) throws IOException {
        URL url = new URL(filial.baseUrl + "/ofertar");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setDoOutput(true);

        byte[] out = mapper.writeValueAsBytes(req);
        con.getOutputStream().write(out);

        int code = con.getResponseCode();
        if (code != 200) throw new IOException("ofertar returned " + code + " from " + filial);
        try (InputStream is = con.getInputStream()) {
            return mapper.readValue(is, OfferResponse.class);
        }
    }

    // POST /confirmarVenda -> returns "true" or "false" (200)
    public boolean callConfirmSale(int orderId, String product) throws IOException {
        URL url = new URL(filial.baseUrl + "/confirmarVenda");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setDoOutput(true);

        ConfirmSaleRequest req = new ConfirmSaleRequest(orderId, product);
        byte[] out = mapper.writeValueAsBytes(req);
        con.getOutputStream().write(out);

        int code = con.getResponseCode();
        if (code != 200) throw new IOException("confirmarVenda returned " + code + " from " + filial);

        try (InputStream is = con.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line = br.readLine();
            return line != null && Boolean.parseBoolean(line.trim());
        }
    }

    // POST /cancelarReserva -> returns 200 OK (no body required)
    public void callCancelReservation(int orderId, String product) throws IOException {
        URL url = new URL(filial.baseUrl + "/cancelarReserva");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setDoOutput(true);

        CancelReservationRequest req = new CancelReservationRequest(orderId, product);
        byte[] out = mapper.writeValueAsBytes(req);
        con.getOutputStream().write(out);

        int code = con.getResponseCode();
        if (code != 200) {
            throw new IOException("cancelarReserva returned " + code + " from " + filial);
        }
    }

    public void replicateFinalPlan(FinalPlanRequest request) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        URL url = new URL(filial.baseUrl + "/replicarPlano");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new IOException("Replication failed: HTTP " + status);
        }
    }

}
