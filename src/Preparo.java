public class Preparo {
    private int id;
    private int comanda;
    private String[] pedido;
    private int tempoPreparo;
    private long inicio;      // timestamp do início
    private boolean pronto;

    public Preparo(int id, int comanda, String[] pedido) {
        this.id = id;
        this.comanda = comanda;
        this.pedido = pedido;
        this.tempoPreparo = (int)(Math.random()) * 3 % 10 + 1;
        this.inicio = System.currentTimeMillis();
        this.pronto = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComanda() {
        return comanda;
    }

    public void setComanda(int comanda) {
        this.comanda = comanda;
    }

    public String[] getPedido() {
        return pedido;
    }

    public void setPedido(String[] pedido) {
        this.pedido = pedido;
    }

    public long getInicio() {
        return inicio;
    }

    public void setInicio(long inicio) {
        this.inicio = inicio;
    }

    public boolean isPronto() {
        return pronto;
    }

    public void setPronto(boolean pronto) {
        this.pronto = pronto;
    }

    public int getTempoPreparo() {
        return tempoPreparo;
    }

    public void setTempoPreparo(int tempoPreparo) {
        this.tempoPreparo = tempoPreparo;
    }

    public boolean estaPronto() {
        long agora = System.currentTimeMillis();
        return (agora - inicio) >= tempoPreparo * 1000;
    }
}
