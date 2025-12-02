package Filial;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FilialState {

    // Basic node identity
    private final String myUrl;
    private boolean isLeader = false;
    private String currentLeader = null;

    // application state
    private Map<Integer, Pedido> pedidos = new HashMap<>();
    private AtomicInteger nextPedidoId = new AtomicInteger(1);

    // Paxos acceptor persistent state (in-memory for assignment)
    private String promisedProposalId = null;   // highest promised proposal id
    private String acceptedProposalId = null;   // highest accepted proposal id
    private String acceptedValue = null;        // value accepted with acceptedProposalId

    // peers (all filiais URLs, including self is okay)
    private final List<String> peers;

    public FilialState(String myUrl, List<String> peers) {
        this.myUrl = myUrl;
        this.peers = new ArrayList<>(peers);
    }

    public String getMyUrl() { return myUrl; }

    // ===== leader control =====
    public synchronized boolean isLeader() { return isLeader; }
    public synchronized void setLeader(String leaderUrl) {
        this.currentLeader = leaderUrl;
        this.isLeader = leaderUrl != null && leaderUrl.equals(myUrl);
        System.out.println("👑 leader set to " + leaderUrl + " (this=" + myUrl + ")");
    }

    public synchronized String getLeader() { return currentLeader; }

    // ===== pedido logic (applied only after commit) =====
    public synchronized int cadastrarPedido(String restaurante) {
        int id = nextPedidoId.getAndIncrement();
        pedidos.put(id, new Pedido(id, restaurante));
        System.out.println("Pedido created locally id=" + id + " by " + myUrl);
        return id;
    }

    public synchronized boolean comprarLocal(int pedidoId, List<String> produtos) {
        Pedido p = pedidos.get(pedidoId);
        if (p == null) return false;
        p.setProdutos(produtos);
        System.out.println("Pedido " + pedidoId + " updated with products on " + myUrl);
        return true;
    }

    public synchronized int tempoEntrega(int pedidoId) {
        Pedido p = pedidos.get(pedidoId);
        if (p == null) return -1;
        return p.getTempoEntrega();
    }

    // ===== Paxos acceptor handlers called by HTTP endpoints =====

    // Prepare: if proposalId >= promisedProposalId -> promise and return any accepted value
    // returns PaxosResponse
    public synchronized PaxosResponse handlePrepare(String proposalId) {
        // If we have already promised to a higher proposal, reject
        if (promisedProposalId != null && compareProposal(promisedProposalId, proposalId) > 0) {
            return new PaxosResponse(PaxosResponse.Status.REJECT, null, null);
        }

        // Otherwise promise not to accept lower proposals
        promisedProposalId = proposalId;

        if (acceptedProposalId != null) {
            // inform proposer of previously accepted proposal/value
            return new PaxosResponse(PaxosResponse.Status.ACCEPTED, acceptedProposalId, acceptedValue);
        } else {
            return new PaxosResponse(PaxosResponse.Status.OK, null, null);
        }
    }

    // Accept: accept proposal if proposalId >= promisedProposalId
    public synchronized boolean handleAccept(String proposalId, String value) {
        if (promisedProposalId != null && compareProposal(promisedProposalId, proposalId) > 0) {
            return false; // reject
        }
        // accept
        promisedProposalId = proposalId;
        acceptedProposalId = proposalId;
        acceptedValue = value;
        System.out.println("▶ Accepted proposal " + proposalId + " value=" + value + " on " + myUrl);
        return true;
    }

    // Commit: apply the value (value is a command string we define)
    public synchronized void handleCommit(String value) {
        // For simplicity, we expect a command string of the form:
        // "COMPRAR:<pedidoId>:p1,p2,p3" or "CADASTRAR:<restaurante>"
        if (value == null || value.trim().isEmpty()) return;
        System.out.println("⤵ Applying committed value on " + myUrl + " : " + value);
        applyCommand(value);
    }

    // Helper: decode and apply the command string to local state
    private void applyCommand(String command) {
        if (command.startsWith("CADASTRAR:")) {
            String restaurante = command.substring("CADASTRAR:".length());
            // create same id? For simplicity we create locally - IDs will diverge unless you make ID assignment part of Paxos
            cadastrarPedido(restaurante);
        } else if (command.startsWith("COMPRAR:")) {
            // Format: COMPRAR:<pedidoId>:p1,p2
            String rest = command.substring("COMPRAR:".length());
            String[] parts = rest.split(":", 2);
            int pedidoId = Integer.parseInt(parts[0]);
            List<String> produtos = parts.length > 1 && !parts[1].isEmpty()
                    ? Arrays.asList(parts[1].split(","))
                    : Collections.emptyList();
            comprarLocal(pedidoId, produtos);
        } else {
            System.out.println("Unknown command: " + command);
        }
    }

    // Simple lexicographic compare of proposal id strings that start with timestamp
    private int compareProposal(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    // ===== Proposer integration (leader uses this) =====

    // The leader uses this to propose a command (string) and commit it via PaxosProposer
    public boolean proposeAndCommit(String command) {
        PaxosProposer proposer = new PaxosProposer(this, peers);
        return proposer.proposeAndCommit(command);
    }
}
