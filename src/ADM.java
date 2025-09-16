import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class ADM implements Restaurante {
    
    private Map<Integer, List<Comanda>> mesas = new HashMap<>();
    private int nextIdComanda = 0;
    private Restaurante stubRestaurante;
    private Cozinha stubCozinha;
    private String[] cardapio;

    public ADM(int qteMesas) {
        for(int i = 0; i < qteMesas; i++) {
            Mesa mesa = new Mesa();
            mesa.setNum(i);
            mesas.put(i, new ArrayList<Comanda>());
        }

        try {
            inicializarCardapio();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setStubCozinha(Cozinha stubCozinha) {
        this.stubCozinha = stubCozinha;
    }

    public void setStubRestaurante(Restaurante stubRestaurante) {
        this.stubRestaurante = stubRestaurante;
    }

    public void inicializarCardapio() throws RemoteException {
        String file = "../menu_restaurante.csv";
        Path filePath = Paths.get(file);
        List<String> cardapio = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                cardapio.add(linha);
            }
        } catch (IOException e) {
            throw new RemoteException("Erro ao ler o cardápio em " + filePath, e);
        }

        this.cardapio = cardapio.toArray(new String[0]);
    }

     @Override
    public int novaComanda(String nome, int idMesa) throws RemoteException {
         System.out.println("Criação de nova comanda solicitada.");
        try {
            // verifica se a mesa existe no mapa
            if (!mesas.containsKey(idMesa)) {
                throw new Exception("A mesa indicada não existe");
            }

            Comanda cm = new Comanda(idMesa, nextIdComanda++, nome);
            mesas.get(idMesa).add(cm);
            return cm.getId();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public String[] consultarCardapio() throws RemoteException {
        System.out.println("Cardápio solicitado.");
        return this.cardapio;
    }
    
    private void incrementaValorComanda(int comanda, float valor) {
        for(List<Comanda> comandas : mesas.values()) {
            for(Comanda c : comandas) {
                if(c.getId() == comanda) { c.setValorAcumulado(valor); return;}
            }
        }
    }

    // Precisa do número da mesa como parâmetro, pois cada mesa tem sua própria lista de comandas...
    @Override
    public String fazerPedido(int comanda, String[] pedido) throws RemoteException {
        System.out.println("Solicitação de pedido recebida.");
        float valor = 0.0f;

        for(String p : pedido) {
            String[] components = p.split(",");
            Float acc = Float.parseFloat(components[2]) ;

            valor += acc;
        }

        int idPreparo = stubCozinha.novoPreparo(comanda, pedido);

        incrementaValorComanda(comanda, valor);

        System.out.println("Pedido número " + String.valueOf(idPreparo) + " realizado.");
        return "Pedido número " + String.valueOf(idPreparo) + "realizado.";
    }

    @Override
    public float valorComanda(int comanda) throws RemoteException {
        System.out.println("Consulta do valor da comanda " + comanda + " solicitada.");
        for (List<Comanda> listaComandas : mesas.values()) {
            for (Comanda c : listaComandas) {
                if(comanda == c.getId()) {
                    return c.getValorAcumulado();
                }
            }
        }
        throw new RuntimeException("Comanda não encontrada.");
    }

    @Override
    public boolean fecharComanda(int comanda) throws RemoteException {
        System.out.println("Fechamento da comanda " + comanda + " solicitado.");
        for (List<Comanda> listaComandas : mesas.values()) {
            boolean removed = listaComandas.removeIf(c -> c.getId() == comanda);
            if (removed) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // Server para Mesa
        try {
            // Instancia o objeto servidor e a sua stub
            int qteMesas = 10;
            ADM server = new ADM(qteMesas);
            Restaurante stubRestaurante = (Restaurante) UnicastRemoteObject.exportObject(server, 0);
            int port = 6602;

            // Registra a stub no RMI Registry para que ela seja obtida pelos clientes
            Registry registryServer = LocateRegistry.createRegistry(port);

            registryServer.bind("Atendimento", stubRestaurante);
            System.out.println("Servidor rodando na porta " + port + "\n" + stubRestaurante );
            server.setStubRestaurante(stubRestaurante);

            // Cliente para Chef
            String host = (args.length < 1) ? null : args[0];            
            Registry registryClient = LocateRegistry.getRegistry(host,6603);
        
            Cozinha stubCozinha = (Cozinha) registryClient.lookup("Preparo");
            server.setStubCozinha(stubCozinha);

            // Cliente para Mercado
            URL url = new URL("http://localhost:9876/WSMercado?wsdl");
			QName qname = new QName(
                "http://localhost:9876/MercadoServidorImplService",
                "MercadoServidorImplService"
            );
 
			Service service = Service.create(url, qname);
            MercadoServidor serverMercado = service.getPort(MercadoServidor.class);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}