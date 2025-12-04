package Filial;

import java.util.*;
import java.io.IOException;

/**
 * Coordinates distributed purchase:
 *  - offer phase (collect offers)
 *  - plan phase (assign each product to a filial)
 *  - commit phase (confirm with chosen filiais)
 *  - rollback if any confirm fails
 */
public class LeaderPurchaseCoordinator {

    private final List<FilialInfo> filiais = Arrays.asList(
        new FilialInfo("F1", "http://localhost:9001"),
        new FilialInfo("F2", "http://localhost:9002"),
        new FilialInfo("F3", "http://localhost:9003"),
        new FilialInfo("F4", "http://localhost:9004"),
        new FilialInfo("F5", "http://localhost:9005")
    ); // deterministic order (used to break ties)
    
    public LeaderPurchaseCoordinator() {}

    /**
     * Attempts to fulfill the requested products for orderId.
     * Returns a map product -> FilialInfo if successful, or null if failed.
     */
    public Map<String, FilialInfo> purchase(int orderId, List<String> products) {

        // 1) OFFER PHASE: ask each filial what it can supply
        Map<FilialInfo, OfferResponse> offers = new LinkedHashMap<>();
        OfferRequest offerReq = new OfferRequest(orderId, products);

        for (FilialInfo f : filiais) {
            FilialHttpClient client = new FilialHttpClient(f);
            try {
                OfferResponse resp = client.callOfertar(offerReq);
                offers.put(f, resp);
            } catch (Exception e) {
                // unreachable or error -> treat as empty offer
                System.out.println("[Leader] filial unreachable: " + f + " -> " + e.getMessage());
                OfferResponse empty = new OfferResponse();
                empty.orderId = orderId;
                empty.filialId = f.id;
                empty.availableProducts = Collections.emptyList();
                offers.put(f, empty);
            }
        }

        // 2) PLAN PHASE: assign each product to a filial
        // Strategy: iterate products in requested order, assign to first filial (by filiais list order) that offered it.
        Map<String, FilialInfo> plan = new HashMap<>();
        for (String product : products) {
            boolean assigned = false;
            for (FilialInfo f : filiais) {
                OfferResponse resp = offers.get(f);
                if (resp != null && resp.availableProducts != null && resp.availableProducts.contains(product)) {
                    plan.put(product, f);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                System.out.println("[Leader] product not available by any filial: " + product);
                // cannot fulfill whole order -> plan fails
                return null;
            }
        }

        // 3) COMMIT PHASE: call confirmarVenda for each (product->filial)
        Map<String, FilialInfo> confirmed = new HashMap<>();
        for (Map.Entry<String, FilialInfo> e : plan.entrySet()) {
            String product = e.getKey();
            FilialInfo f = e.getValue();
            FilialHttpClient client = new FilialHttpClient(f);
            try {
                boolean ok = client.callConfirmSale(orderId, product);
                if (ok) {
                    confirmed.put(product, f);
                } else {
                    System.out.println("[Leader] confirmSale FAILED for " + product + " at " + f);
                    // rollback already confirmed
                    rollbackConfirmed(orderId, confirmed);
                    return null;
                }
            } catch (IOException ex) {
                System.out.println("[Leader] confirmSale exception for " + product + " at " + f + " -> " + ex.getMessage());
                rollbackConfirmed(orderId, confirmed);
                return null;
            }
        }

        // success: all confirmed.. time to replicate the log
        FinalPlanRequest finalReq = new FinalPlanRequest();
        finalReq.orderId = orderId;

        Map<String, String> flatPlan = new HashMap<>();
        for (Map.Entry<String, FilialInfo> e : plan.entrySet()) {
            flatPlan.put(e.getKey(), e.getValue().id);
        }
        finalReq.plan = flatPlan;

        // Broadcast to all filiais (including non-sellers)
        for (FilialInfo f : filiais) {
            FilialHttpClient client = new FilialHttpClient(f);
            try {
                client.replicateFinalPlan(finalReq);
            } catch (IOException ex) {
                System.out.println("[Leader] replication failed for " + f +
                        " -> " + ex.getMessage());
            }
        }
        
        System.out.println("[Leader] purchase successful for order " + orderId + " plan=" + plan);
        return plan;
    }

    // helper to rollback already confirmed products
    private void rollbackConfirmed(int orderId, Map<String, FilialInfo> confirmed) {
        for (Map.Entry<String, FilialInfo> e : confirmed.entrySet()) {
            String product = e.getKey();
            FilialInfo f = e.getValue();
            FilialHttpClient client = new FilialHttpClient(f);
            try {
                client.callCancelReservation(orderId, product);
            } catch (IOException ex) {
                // log and continue: rollback best-effort
                System.out.println("[Leader] rollback failed for " + product + " at " + f + " -> " + ex.getMessage());
            }
        }
        System.out.println("[Leader] rollback completed for order " + orderId);
    }
}
