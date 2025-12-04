package Filial;

public class ConfirmSaleRequest {
    public int orderId;
    public String product;

    public ConfirmSaleRequest() {}
    public ConfirmSaleRequest(int orderId, String product) {
        this.orderId = orderId;
        this.product = product;
    }
}
