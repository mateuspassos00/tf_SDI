package Mercado;

import java.util.*;
import Filial.FilialClient;

public class LeaderLocator {

    // ✅ List of all Filiais in the system
    private final List<String> filiais = Arrays.asList(
            "http://localhost:9001",
            "http://localhost:9002",
            "http://localhost:9003",
            "http://localhost:9004",
            "http://localhost:9005"
    );

    private FilialClient cachedLeader = null;
    private long lastCheckTime = 0;

    // how often we re-check leader health (ms)
    private static final long CHECK_INTERVAL = 3000;

    // ===============================
    // ✅ Main public method
    // ===============================
    public synchronized FilialClient getLeaderClient() {

        long now = System.currentTimeMillis();

        // ✅ If cached leader is fresh, reuse it
        if (cachedLeader != null && (now - lastCheckTime) < CHECK_INTERVAL) {
            return cachedLeader;
        }

        System.out.println("🔍 Searching for leader among filiais...");

        // ✅ Look for the leader
        for (String url : filiais) {
            try {
                FilialClient client = new FilialClient(url);
                if (client.isLeader()) {
                    cachedLeader = client;
                    lastCheckTime = now;
                    System.out.println("✅ Leader found at " + url);
                    return client;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Filial offline: " + url);
            }
        }

        // ✅ If we reach here → no leader exists
        cachedLeader = null;
        throw new RuntimeException("❌ No leader available in the system!");
    }

    // ===============================
    // ✅ Force refresh (used on errors)
    // ===============================
    public synchronized void invalidateLeader() {
        cachedLeader = null;
        lastCheckTime = 0;
    }
}
