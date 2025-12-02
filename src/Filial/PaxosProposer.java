package Filial;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PaxosProposer {

    private final FilialState state;
    private final List<String> peers; // includes leader's own url optionally
    private final int timeoutMs = 2000;

    public PaxosProposer(FilialState state, List<String> peers) {
        this.state = state;
        this.peers = new ArrayList<>(peers);
    }

    // Propose a command (arbitrary string) and commit if majority
    public boolean proposeAndCommit(String command) {
        String proposalId = makeProposalId();

        // Phase 1: Prepare -> collect promises from majority
        int promises = 0;
        String highestAcceptedValue = null;
        String highestAcceptedId = null;

        for (String peer : peers) {
            try {
                String payload = proposalId;
                String resp = post(peer + "/paxos/prepare", payload);
                // Response format: OK or REJECT or ACCEPTED:<acceptedId>:<acceptedValue>
                if (resp == null) continue;
                if (resp.startsWith("ACCEPTED:")) {
                    // format ACCEPTED:<acceptedId>:<acceptedValue>
                    String[] parts = resp.split(":", 3);
                    String accId = parts[1];
                    String accVal = parts.length >= 3 ? parts[2] : null;
                    if (highestAcceptedId == null || accId.compareTo(highestAcceptedId) > 0) {
                        highestAcceptedId = accId;
                        highestAcceptedValue = accVal;
                    }
                    promises++;
                } else if (resp.equals("OK")) {
                    promises++;
                } else if (resp.equals("REJECT")) {
                    // do nothing
                }
            } catch (Exception e) {
                // peer unreachable — treated as no promise
            }
        }

        int majority = (peers.size() / 2) + 1;
        if (promises < majority) {
            System.out.println("❌ Paxos prepare failed: promises=" + promises + " < majority=" + majority);
            return false;
        }

        // If some acceptor already had an accepted value with highest id, we must use it.
        String valueToPropose = highestAcceptedValue != null ? highestAcceptedValue : command;

        // Phase 2: Accept -> ask majority to accept (proposalId, value)
        int accepts = 0;
        for (String peer : peers) {
            try {
                String payload = proposalId + "|" + valueToPropose;
                String resp = post(peer + "/paxos/accept", payload);
                // Response: ACCEPTED or REJECT
                if ("ACCEPTED".equals(resp)) accepts++;
            } catch (Exception e) {
                // unreachable
            }
        }

        if (accepts < majority) {
            System.out.println("❌ Paxos accept failed: accepts=" + accepts + " < majority=" + majority);
            return false;
        }

        // Phase 3: Commit (notify all learners / followers to apply)
        int commits = 0;
        for (String peer : peers) {
            try {
                String payload = valueToPropose;
                String resp = post(peer + "/paxos/commit", payload);
                if ("OK".equals(resp)) commits++;
            } catch (Exception e) {
                // unreachable
            }
        }

        // We consider success if majority committed (learners may apply later as well)
        if (commits >= majority) {
            System.out.println("✅ Paxos commit succeeded; commits=" + commits);
            return true;
        } else {
            System.out.println("⚠️ Paxos commit partial: commits=" + commits);
            // still OK to return true because majority accepted earlier; but to be safe return based on commits
            return commits >= majority;
        }
    }

    private String makeProposalId() {
        return System.currentTimeMillis() + "-" + state.getMyUrl();
    }

    private String post(String urlString, String payload) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
