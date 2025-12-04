package Filial;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class LeaderMonitor extends Thread {

    private final FilialState state;
    private final List<String> filiais = Arrays.asList(
        "http://localhost:9001",
        "http://localhost:9002",
        "http://localhost:9003",
        "http://localhost:9004",
        "http://localhost:9005"
    );

    public LeaderMonitor(FilialState state) {
        this.state = state;
        setDaemon(true);
    }

    @Override
    public void run() {
    try {
        Thread.sleep(1000); // give time for all servers to boot
    } catch (InterruptedException ignored) {}

    // ✅ BOOTSTRAP ELECTION
    if (state.getLeaderUrl() == null) {
        System.out.println("⚡ No leader at startup, starting election...");
        startElection();
    }

    // ✅ NORMAL OPERATION
    while (true) {
        try {
            Thread.sleep(2000);

            if (state.isLeader()) continue;

            if (!pingLeader()) {
                String possibleLeader = discoverLeader();
                if(possibleLeader != null) {
                    state.setLeader(possibleLeader);                    
                } else {
                    System.out.println("⚠️ Leader down! Starting election...");
                    startElection();
                }                                
            }

        } catch (Exception ignored) {}
    }
}


    private boolean pingLeader() {
        try {
            URL url = new URL(state.getLeaderUrl() + "/isLeader");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(800);
            conn.setReadTimeout(800);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void startElection() {
        boolean strongerExists = false;

        for (String f : filiais) {
            int otherPort = Integer.parseInt(f.split(":")[2]);

            if (otherPort > state.getMyPort()) {
                try {
                    URL url = new URL(f + "/election");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(800);
                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        strongerExists = true;
                    }

                } catch (Exception ignored) {}
            }
        }

        if (!strongerExists) {
            becomeLeader();
        }
    }

    private String discoverLeader() {
        for (String f : filiais) {
            try {
                URL url = new URL(f + "/isLeader");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(800);
                conn.setReadTimeout(800);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );
                    String response = br.readLine();

                    if ("true".equalsIgnoreCase(response)) {
                        System.out.println("✅ Leader found at: " + f);
                        return f;
                    }
                }

            } catch (Exception ignored) {
                // Filial offline or unreachable
            }
        }

        return null; // no leader found
    }
    
    private void becomeLeader() {
        System.out.println("👑 I AM THE NEW LEADER: " + state.getFilialUrl());
        state.setLeader(state.getFilialUrl());

        // ANNOUNCE TO ALL
        for (String f : filiais) {
            try {
                URL url = new URL(f + "/setLeader");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.getOutputStream().write(state.getFilialUrl().getBytes());
                conn.connect();
            } catch (Exception ignored) {}
        }
    }
}
