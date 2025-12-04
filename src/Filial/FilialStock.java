package Filial;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilialStock {

    // product -> quantity
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public void loadFromCSV(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String product = parts[0].trim();
            int quantity = Integer.parseInt(parts[1].trim());
            stock.put(product, quantity);
        }
        br.close();
    }

    public synchronized boolean hasProduct(String product) {
        return stock.getOrDefault(product, 0) > 0;
    }

    public synchronized boolean removeOne(String product) {
        int qty = stock.getOrDefault(product, 0);
        if (qty > 0) {
            stock.put(product, qty - 1);
            return true;
        }
        return false;
    }

    public synchronized void addOneBack(String product) {
        stock.put(product, stock.getOrDefault(product, 0) + 1);
    }

    public Map<String, Integer> snapshot() {
        return new HashMap<>(stock);
    }
}
