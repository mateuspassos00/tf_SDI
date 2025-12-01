package Mercado;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

// import Cozinha.Cozinha;
// import Mercado.MercadoServidor;
// import Mesa.Mesa;

public class ADM implements Restaurante {
    
    // Parte 1 do trabalho
    private Map<Integer, List<Comanda>> mesas = new HashMap<>();
    private int nextIdComanda = 0;
    private Restaurante stubRestaurante;
    private Cozinha stubCozinha;
    private String[] cardapio;

    // Parte 2 do trabalho
    public static String nomeRestaurante = "Restaurante Mateus & Matheus LTDA";
    private MercadoServidor serverMercado;
    private int numPedidoAtual;

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

    public int getNumPedidoAtual() {
        return numPedidoAtual;
    }

    public void setNumPedidoAtual(int numPedidoAtual) {
        this.numPedidoAtual = numPedidoAtual;
    }

    public void setStubCozinha(Cozinha stubCozinha) {
        this.stubCozinha = stubCozinha;
    }

    public void setStubRestaurante(Restaurante stubRestaurante) {
        this.stubRestaurante = stubRestaurante;
    }

    public void inicializarCardapio() throws RemoteException {
        String file = "menu_restaurante.csv";
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
            Registry registryClient = LocateRegistry.getRegistry(host,1098);
        
            Cozinha stubCozinha = (Cozinha) registryClient.lookup("Cozinha");
            server.setStubCozinha(stubCozinha);

            // Cliente para Mercado
            URL url = new URL("http://localhost:9877/mercado?wsdl");
			QName qname = new QName(
                "http://Mercado/",
                "MercadoService"
            );
 
			Service service = Service.create(url, qname);
            MercadoServidor serverMercado = service.getPort(MercadoServidor.class);

            Scanner scan = new Scanner(System.in);

            while (true) {
                System.out.println("\n==== MENU ADM ====");
                System.out.println("1 - Iniciar um novo pedido no mercado");
                System.out.println("2 - Comprar produtos");
                System.out.println("3 - Consultar tempo de entrega de um pedido");
                System.out.println("9 - Sair");
                System.out.print("Escolha: ");

                int op = scan.nextInt();
                scan.nextLine();

                switch (op) {
                    case 1:
                        server.setNumPedidoAtual(serverMercado.cadastrarPedido(nomeRestaurante));
                        break;
                    case 2:
                        String pedidos[] = {"Alface", "Batata", "Cebola", "Hamburguer", "Pao", "Maionese"};
                        int idPedido = server.getNumPedidoAtual();
                        serverMercado.comprarProdutos(idPedido, pedidos);
                        break;
                    case 3:
                        int tempoEntrega = serverMercado.tempoEntrega(server.getNumPedidoAtual());
                        System.out.println("Tempo de entrega estimado: " + tempoEntrega + "horas");
                        break;
                    case 9:
                        System.out.println("Saindo.");
                        scan.close();
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }

                if(op == 9) break;
            }            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
