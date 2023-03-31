import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class DSClient {
    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static final int portNum = 50000;

    static Socket socket;
    static BufferedReader reader;
    static DataOutputStream outStream;
    static Scanner scanner;

    static String largestServerType;
    static int largestCoreCount = 0;

    static ArrayList<Integer> serverList = new ArrayList<Integer>();
    static int currServerIdx = 0;

    static void sendMessage(String msg) throws IOException {
        outStream.write((msg+"\n").getBytes());
        outStream.flush();
        // System.out.println("C SENT: " + msg);
    }

    static String readMessage() throws IOException {
        String msg = reader.readLine();
        // System.out.println("C RCVD: " + msg);
        return msg;
    }

    static void quitProgram() throws IOException {
        sendMessage("QUIT");
        readMessage();

        scanner.close();
        reader.close();
        outStream.close();
        socket.close();
    }

    static void getLargestServer() throws IOException {
        // get server state information
        sendMessage("GETS All");
        scanner = new Scanner(readMessage());
        scanner.next(); // skip DATA
        int serverCount = scanner.nextInt();
        scanner.close();

        sendMessage("OK");

        // gotta pick the server that comes first with the largest number of cores
        /* name     core   memory
         * medium    4      100
         * large     4      200
         */
        // gotta pick medium not large

        String serverType;
        int coreCount;
        int serverID;

        for (int i = 0; i < serverCount; i++) {
            scanner = new Scanner(readMessage());
            serverType = scanner.next();
            serverID = scanner.nextInt();
            scanner.next(); // skip state
            scanner.next(); // skip curStartTime;
            coreCount = scanner.nextInt();

            if (coreCount > largestCoreCount) {
                serverList.clear();
                serverList.add(serverID);
                largestCoreCount = coreCount;
                largestServerType = serverType;
            } else if (serverType.equals(largestServerType)) {
                serverList.add(serverID);
            }
            scanner.close();
        }

        sendMessage("OK");
        readMessage(); // receive "."
    }

    public static void main(String args[]) throws IOException {
        socket = new Socket(IP_ADDRESS, portNum);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outStream = new DataOutputStream(socket.getOutputStream());

        // Handshake
        // Send HELO
        sendMessage("HELO");
        // Receive OK first time
        readMessage();
        sendMessage("AUTH " + AUTH_INFO);
        // Receive OK second time
        readMessage();
        // Send ready message
        sendMessage("REDY");

        // Receive JOBN
        readMessage();

        getLargestServer();

        sendMessage("REDY");

        String command;
        mainLoop: while (true) {
            scanner = new Scanner(readMessage());
            command = scanner.next();

            switch (command) {
                case "NONE":
                    break mainLoop;
                case "OK":
                    sendMessage("REDY");
                    break;
                case "JOBN":
                    scanner.nextInt(); // skip sumbitTime
                    int jobID = scanner.nextInt();
                    // int estRunTime = scanner.nextInt();
                    // int core = scanner.nextInt();
                    // int memory = scanner.nextInt();
                    // int disk = scanner.nextInt();

                    // LRR algorithm
                    int serverID = serverList.get(currServerIdx);
                    String msgToSend = "SCHD " + jobID + " " + largestServerType + " " +serverID;
                    currServerIdx = (currServerIdx + 1) % serverList.size();
                    sendMessage(msgToSend);
                    break;
                case "JCPL":
                    sendMessage("REDY");
                    break;
                // case "ERR":
                //     break mainLoop;
                // case "JOBP":
                //     break mainLoop;
                // case "RESF":
                //     break mainLoop;
                // case "RESR":
                //     break mainLoop;
                // case "CHKQ":
                //     break mainLoop;
                default:
                    break mainLoop;
            }
            scanner.close();
        }
        quitProgram();
    }
}