import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class DSClient {
    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static int portNum = 50000;

    static Socket socket;

    static void sendMessage(DataOutputStream outStream, String msg) throws IOException {
        outStream.write((msg+"\n").getBytes());
        outStream.flush();
        System.out.println("SENT: " + msg);
    }

    static String readMessage(BufferedReader inStream) throws IOException {
        String msg = inStream.readLine();
        System.out.println("RCVD: " + msg);
        return msg;
    }

    public static void main(String args[]) throws Exception {
        socket = new Socket(IP_ADDRESS, portNum);
        BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

        String inString = "";

        // Send HELO
        sendMessage(outStream, "HELO");

        // Receive OK first time
        inString = readMessage(inStream);

        sendMessage(outStream, "AUTH " + AUTH_INFO);

        // Receive OK second time
        inString = readMessage(inStream);

        sendMessage(outStream, "REDY");

        readMessage(inStream);

        // quit early
        sendMessage(outStream, "QUIT");

        readMessage(inStream);
        // loop1:
        // while(true){
        //     // outStr=br.readLine();
        //     // dout.writeUTF(outStr);
        //     // dout.flush();

        //     inString=inStream.readUTF();
        //     System.out.println(inString);
        //     switch (inString) {
        //         case "OK":
        //             outStream.writeUTF("AUTH isac");
        //             outStream.flush();
        //             break;
        //         case "BYE":
        //             break loop1;
        //     }
        // }

        inStream.close();
        outStream.close();
        socket.close();
    }
}