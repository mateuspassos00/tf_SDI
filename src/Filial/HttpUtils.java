package Filial;

import java.io.*;

public class HttpUtils {

    public static String readRequestBody(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return new String(buffer.toByteArray(), "UTF-8");
    }
}
