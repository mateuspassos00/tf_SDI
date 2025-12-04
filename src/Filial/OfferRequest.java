package Filial;

import java.util.List;

public class OfferRequest {
    public int orderId;
    public List<String> requestedProducts;

    public OfferRequest() {}
    public OfferRequest(int orderId, List<String> requestedProducts) {
        this.orderId = orderId;
        this.requestedProducts = requestedProducts;
    }
}
