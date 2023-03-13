import java.net.Socket;
import java.io.*;

public class DSClient {
    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static int portNum = 50000;

    static Socket socket;
    static DataInputStream inStream;
    static DataOutputStream outStream;

    static void sendMessage(String msg) throws IOException {
        outStream.writeUTF(msg);
        outStream.flush();
        System.out.println("(Client) Sent: " + msg);
    }

    static String readMessage() throws IOException {
        String msg = inStream.readUTF();
        System.out.println("(Server) Received: " + msg);
        return msg;
    }

    public static void main(String args[]) throws Exception {
        socket = new Socket(IP_ADDRESS, portNum);
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());

        String inString = "";

        // Send HELO
        sendMessage("HELO");

        // Receive OK first time
        inString = readMessage();
        if (inString.equals("OK")) {
            sendMessage("AUTH" + AUTH_INFO);
        } else {
            System.out.println("Server not OK");
            outStream.close();
            socket.close();
            return;
        }

        // Receive OK second time
        inString = readMessage();
        if (inString.equals("OK")) {
            sendMessage("REDY");
        } else {
            System.out.println("Server not OK");
            outStream.close();
            socket.close();
            return;
        }

        readMessage();

        // quit early
        sendMessage("QUIT");

        readMessage();
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