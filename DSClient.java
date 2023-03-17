import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class DSClient {
    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static int portNum = 50000;

    static void sendMessage(DataOutputStream outStream, String msg) throws IOException {
        outStream.write((msg+"\n").getBytes());
        outStream.flush();
        System.out.println("SENT: " + msg);
    }

    static String readMessage(BufferedReader reader) throws IOException {
        String msg = reader.readLine();
        System.out.println("RCVD: " + msg);
        return msg;
    }

    public static void main(String args[]) throws Exception {
        Socket socket = new Socket(IP_ADDRESS, portNum);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

        String inString = "";

        // Handshake
        // Send HELO
        sendMessage(outStream, "HELO");
        // Receive OK first time
        inString = readMessage(reader);
        sendMessage(outStream, "AUTH " + AUTH_INFO);
        // Receive OK second time
        inString = readMessage(reader);
        sendMessage(outStream, "REDY");
        readMessage(reader);

        // quit early
        sendMessage(outStream, "QUIT");
        readMessage(reader);

        reader.close();
        outStream.close();
        socket.close();
    }
}