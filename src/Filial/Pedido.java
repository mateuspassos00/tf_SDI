package Filial;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Pedido {
    private int id;
    private String restaurante;
    private Map<String, String> produtosPorFilial = new HashMap<>();
    private List<String> produtos;
    private int tempoEntrega;

    public Pedido(int id, String restaurante) {
        this.id = id;
        this.restaurante = restaurante;
        Random rand = new Random();
        this.tempoEntrega = rand.nextInt(60); // gerado aleatoriamente ao criar pedido
    }

    public void registrarAquisicao(String produto, String filial) {
        produtosPorFilial.put(produto, filial);
    }

    public Map<String, String> getResultado() {
        return produtosPorFilial;
    }

    public boolean concluido(String[] produtos) {
        return produtosPorFilial.keySet().containsAll(Arrays.asList(produtos));
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