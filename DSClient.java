import java.io.IOException;
import java.net.*;

public class DSClient {
    Socket clientSocket;

    DSClient() {
        try {
            // default port 50000
            clientSocket = new Socket("127.0.0.1", 6666);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
    }
}
