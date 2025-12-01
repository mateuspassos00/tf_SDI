package Mercado;

import java.util.Arrays;
import java.util.List;

public class LeaderLocator {

    private List<String> filialUrls = Arrays.asList(
        "http://localhost:9001",
        "http://localhost:9002",
        "http://localhost:9003"
    );

    public FilialClient getLeaderClient() {
        for (String url : filialUrls) {
            boolean isLeader = Http.get(url + "/isLeader", boolean.class);
            if (isLeader) return new FilialClient(url);
        }
        throw new RuntimeException("No leader found!");
    }
}
