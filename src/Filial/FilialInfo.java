package Filial;

public class FilialInfo {
    public final String id;
    public final String baseUrl; // e.g. "http://localhost:9001"

    public FilialInfo(String id, String baseUrl) {
        this.id = id;
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return id + "@" + baseUrl;
    }
}
