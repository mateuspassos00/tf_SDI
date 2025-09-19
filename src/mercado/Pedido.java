package mercado;

import java.util.List;
import java.util.Random;

public class Pedido {
    private int id;
    private String restaurante;
    private List<String> produtos;
    private int tempoEntrega;

    public Pedido(int id, String restaurante) {
        this.id = id;
        this.restaurante = restaurante;
        Random rand = new Random();
        this.tempoEntrega = rand.nextInt(60);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getRestaurante() {
        return restaurante;
    }
    public void setRestaurante(String restaurante) {
        this.restaurante = restaurante;
    }
    public List<String> getProdutos() {
        return produtos;
    }
    public void setProdutos(List<String> produtos) {
        this.produtos = produtos;
    }
    public int getTempoEntrega() {
        return tempoEntrega;
    }
    public void setTempoEntrega(int tempoEntrega) {
        this.tempoEntrega = tempoEntrega;
    }
    
}
