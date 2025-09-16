public class Comanda {
    private float valorAcumulado;
    private int mesa;
    private int id;
    private String nome;
    boolean paid;

    public Comanda(int mesa, int id, String nome) {
        this.mesa = mesa;
        this.id = id;
        this.nome = nome;
        this.paid = false;
        this.valorAcumulado = 0.0f;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getMesa() {
        return mesa;
    }
    public void setMesa(int mesa) {
        this.mesa = mesa;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public float getValorAcumulado() {
        return valorAcumulado;
    }

    public void setValorAcumulado(float valorAcumulado) {
        this.valorAcumulado += valorAcumulado;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

}
