package mercado;
import javax.xml.ws.Endpoint;

public class MercadoServidorPublisher {

	public static void main(String[] args) {
		System.out.println("Beginning to publish MercadoService now");
		Endpoint.publish("http://127.0.0.1:9876/WSMercado", new MercadoServidorImpl());
		System.out.println("Done publishing");
	}

}