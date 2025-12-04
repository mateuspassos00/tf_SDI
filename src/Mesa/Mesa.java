package Mesa;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ADM.Restaurante;

// import ADM.Restaurante;

public class Mesa {

    private int num;
    private Restaurante stub;
    private List<Integer> comandas = new ArrayList<Integer>();
    
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Restaurante getStub() {
        return stub;
    }

    public void setStub(Restaurante stub) {
        this.stub = stub;
    }

    public void initRegistry(String host, int port) {
        try {
            
            // Obtém uma referência para o registro do RMI
            Registry registry = LocateRegistry.getRegistry(host,port);

            // Obtém a stub do servidor
            Restaurante stubR = (Restaurante) registry.lookup("Restaurante");
            this.setStub(stubR);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void pedirCardapio () throws RemoteException{
        String[] cardapio = stub.consultarCardapio();
        for(String prato : cardapio) System.out.println(prato);
    }

    public void criarComanda(String nome, int numMesa) throws RemoteException{
        int codComanda = stub.novaComanda(nome, numMesa);

        if(codComanda != -1) {
            this.comandas.add(codComanda);
            System.out.println("Nova comanda criada com o código " + codComanda);
            return;
        }
        System.out.println("Erro ao criar a comanda");
    }
    
    public void fazerPedido(int comanda, String[] pedido) throws RemoteException{
        stub.fazerPedido(comanda, pedido);
    }
    
    public void consultarValorComanda(int comanda) throws RemoteException {
        float valor = stub.valorComanda(comanda);
        System.out.println("Valor a ser pago: $" + valor);
    }

    public void fecharComanda(int comanda) throws RemoteException {
        if (stub.fecharComanda(comanda)) {
            comandas.removeIf(c -> c == comanda);
        }
    }

    public void menu() {
        Scanner sc = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n=== MENU MESA " + this.num + " ===");
            System.out.println("1 - Consultar cardápio");
            System.out.println("2 - Criar nova comanda");
            System.out.println("3 - Fazer pedido");
            System.out.println("4 - Consultar valor da comanda");
            System.out.println("5 - Fechar comanda");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            opcao = sc.nextInt();
            sc.nextLine();

            try {
                switch (opcao) {
                    case 1:
                        pedirCardapio();
                        break;

                    case 2:
                        System.out.print("Nome do cliente: ");
                        String nome = sc.nextLine();
                        criarComanda(nome, this.num);
                        break;

                    case 3:
                        if (comandas.isEmpty()) {
                            System.out.println("Nenhuma comanda aberta!");
                            break;
                        }
                        System.out.print("Código da comanda: ");
                        int comandaPedido = sc.nextInt();
                        sc.nextLine();

                        System.out.println("Digite o pedido (Ex: '1,Feijoada,35.0'), 'fim' para encerrar:");
                        List<String> pedidos = new ArrayList<>();
                        String linha;
                        while (!(linha = sc.nextLine()).equalsIgnoreCase("fim")) {
                            pedidos.add(linha);
                        }
                        fazerPedido(comandaPedido, pedidos.toArray(new String[0]));
                        break;

                    case 4:
                        if (comandas.isEmpty()) {
                            System.out.println("Nenhuma comanda aberta.");
                            break;
                        }
                        System.out.print("Código da comanda: ");
                        int codConsulta = sc.nextInt();
                        sc.nextLine();
                        consultarValorComanda(codConsulta);
                        break;

                    case 5:
                        if (comandas.isEmpty()) {
                            System.out.println("Nenhuma comanda aberta.");
                            break;
                        }
                        System.out.print("Código da comanda: ");
                        int codFechar = sc.nextInt();
                        sc.nextLine();
                        fecharComanda(codFechar);
                        System.out.println("Comanda " + codFechar + " fechada.");
                        break;

                    case 0:
                        System.out.println("Volte sempre!");
                        break;

                    default:
                        System.out.println("Opção inválida!");
                }
            } catch (RemoteException e) {
                System.out.println("Erro remoto: " + e.getMessage());
                e.printStackTrace();
            }
        } while (opcao != 0);

        sc.close();
    }

    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        
        Scanner sc = new Scanner(System.in);
        System.out.print("Digite o número da mesa: ");
        int numMesa = sc.nextInt();
        sc.nextLine();

        Mesa mesa = new Mesa();
        mesa.setNum(numMesa);

        mesa.initRegistry(host, 1097);
        System.out.println(mesa.getStub());
        
        try {
            mesa.menu();

        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            sc.close();
        }
    }    
}   