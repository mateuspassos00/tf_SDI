package Filial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/filial")
public class FilialController {

    private final FilialStock stock = new FilialStock();
    private final String filialId;

    // Tracks temporary reservations: orderId -> list of products
    private final Map<Integer, List<String>> reservations = new ConcurrentHashMap<>();

    public FilialController(String filialId, String csvPath) throws IOException {
        this.filialId = filialId;
        stock.loadFromCSV(csvPath);
    }

    // ========================
    // 1️⃣ /ofertar
    // ========================
    @POST
    @Path("/ofertar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OfferResponse offerProducts(OfferRequest request) {

        List<String> available = new ArrayList<>();

        for (String p : request.requestedProducts) {
            if (stock.hasProduct(p)) {
                available.add(p);
            }
        }

        OfferResponse response = new OfferResponse();
        response.orderId = request.orderId;
        response.filialId = filialId;
        response.availableProducts = available;

        System.out.println("[" + filialId + "] Offering: " + available);

        return response;
    }

    // ========================
    // 2️⃣ /confirmarVenda
    // ========================
    @POST
    @Path("/confirmarVenda")
    @Consumes(MediaType.APPLICATION_JSON)
    public synchronized boolean confirmSale(ConfirmSaleRequest request) {

        boolean success = stock.removeOne(request.product);

        if (success) {
            reservations
                .computeIfAbsent(request.orderId, k -> new ArrayList<>())
                .add(request.product);

            System.out.println("[" + filialId + "] Confirmed sale: " + request.product);
        } else {
            System.out.println("[" + filialId + "] FAILED sale: " + request.product);
        }

        return success;
    }

    // ========================
    // 3️⃣ /cancelarReserva
    // ========================
    @POST
    @Path("/cancelarReserva")
    @Consumes(MediaType.APPLICATION_JSON)
    public synchronized void cancelReservation(CancelReservationRequest request) {

        List<String> products = reservations.get(request.orderId);
        if (products != null && products.remove(request.product)) {
            stock.addOneBack(request.product);
            System.out.println("[" + filialId + "] Reservation canceled: " + request.product);
        }
    }
}
