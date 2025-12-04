package Cozinha;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.List;

public class Chef implements Cozinha {
    public Chef() {}

    private int idNextPreparo = 0;
    private List<Preparo> preparos = new ArrayList<Preparo>();
    
     @Override
    public int novoPreparo(int comanda, String[] pedido) throws RemoteException {
        Preparo p = new Preparo(idNextPreparo, comanda, pedido);
        preparos.add(p);
         System.out.println("Preparo número " + p.getId() + " criado.");
        idNextPreparo++;

        return idNextPreparo;
    }

    @Override
    public int tempoPreparo(int preparo) throws RemoteException {
        System.out.println("Consulta do tempo do preparo " + preparo + " solicitada.");
         for (Preparo p : preparos) {
            if (p.getId() == preparo) return p.getTempoPreparo();
        }
        return -1;
    }


    @Override
    public String[] pegarPreparo(int preparo) throws RemoteException {
        System.out.println("Retirada do preparo " + preparo + " solicitada.");
         Preparo p = null;
        String[] str = new String[1];
        for (Preparo p1 : preparos) {if (p1.getId() == preparo) p = p1;}
        if (p == null) {
            str[0] = "Pedido não existe.";
            return str;
        }
        else {
            if(p.estaPronto()) return p.getPedido();
            else {
                str[0] = "Pedido não está pronto.";
                return str;
            }
        }
    }

    public static void main(String[] args) {
        // Server para ADM
        try {
            // Instancia o objeto servidor e a sua stub
            Chef server = new Chef();
            Cozinha stub = (Cozinha) UnicastRemoteObject.exportObject(server, 0);

            // Registra a stub no RMI Registry para que ela seja obtida pelos clientes
            int port = 6603;
            Registry registry = LocateRegistry.createRegistry(port);

            registry.bind("Cozinha", stub);
            System.out.println("Servidor rodando na porta " + port + "\n" + stub );
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
   
}