import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.File;
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
        Scanner scanner;

        String msg;
        String arg;
        // Handshake
        // Send HELO
        sendMessage(outStream, "HELO");
        // Receive OK first time
        readMessage(reader);
        sendMessage(outStream, "AUTH " + AUTH_INFO);
        // Receive OK second time
        readMessage(reader);

        sendMessage(outStream, "REDY");

        sendMessage(outStream, "GETS all");
        msg = readMessage(reader);
        scanner = new Scanner(msg);
        scanner.next(); // skip DATA

        int serverNum = scanner.nextInt();

        scanner.close();
        sendMessage(outStream, "OK");
        for (int i = 0; i < serverNum - 1; i++) {
            readMessage(reader);
        }
        msg = readMessage(reader);
        scanner = new Scanner(msg);

        String largestServerType = scanner.next();
        int largestServerLimit = scanner.nextInt() + 1;

        scanner.close();

        int curServerID = 0;

        mainLoop: while (true) {
            msg = readMessage(reader);
            scanner = new Scanner(msg);
            arg = scanner.next();

            switch (arg) {
                case "NONE":
                    break mainLoop;
                case "JOBN":
                    int sumbitTime = scanner.nextInt();
                    int jobID = scanner.nextInt();
                    // int estRunTime = scanner.nextInt();
                    // int core = scanner.nextInt();
                    // int memory = scanner.nextInt();
                    // int disk = scanner.nextInt();
                    String msgToSend = "SCHD" + jobID + largestServerType + curServerID;
                    curServerID = (curServerID + 1) % largestServerLimit;
                    sendMessage(outStream, msgToSend);
                    break;
                case "JOBP":
                    break mainLoop;
                case "JCPL":
                    sendMessage(outStream, "REDY");
                    break;
                case "RESF":
                    break mainLoop;
                case "RESR":
                    break mainLoop;
                case "CHKQ":
                    break mainLoop;
            }
            scanner.close();
        }

        // quit
        sendMessage(outStream, "QUIT");
        readMessage(reader);

        reader.close();
        outStream.close();
        socket.close();
    }
}