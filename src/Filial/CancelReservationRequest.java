package Filial;

public class CancelReservationRequest {
    public int orderId;
    public String product;

    public CancelReservationRequest() {}
    public CancelReservationRequest(int orderId, String product) {
        this.orderId = orderId;
        this.product = product;
    }
}
