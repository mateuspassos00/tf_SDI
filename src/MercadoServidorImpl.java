import javax.jws.WebService;
import java.util.ArrayList;
 
@WebService(endpointInterface = "MercadoServidor")
public class MercadoServidorImpl implements MercadoServidor {
    // private int idNextPedido = 0;
    // private ArrayList<String> pedidos = new ArrayList<String>();
 
	public int cadastrarPedido(String restaurante) {        
        return 1;
    }

    public boolean comprarProdutos(int restaurante, String[] produtos) {
        return true;
    }

    public int tempoEntrega(int restaurante) {
        return 1;
    }
 
}