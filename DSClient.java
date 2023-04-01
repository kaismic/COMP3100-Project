import java.net.Socket;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class DSClient {
    enum JOBN {
        command, submitTime, jobID, estRunTime, core, memory;
    }

    enum DATA {
        command, nRecs, recLen;
    }

    enum GETS {
        serverType, serverID, state, curStartTime, core, memory, disk, waitingJobNum, runningJobNum;
    }

    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static final int portNum = 50000;

    static Socket socket;
    static BufferedReader reader;
    static DataOutputStream outStream;

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

    static int atoi(String str) {
        return Integer.parseInt(str);
    }

    static void quitProgram() throws IOException {
        sendMessage("QUIT");
        readMessage();

        reader.close();
        outStream.close();
        socket.close();
    }

    static void getLargestServer() throws IOException {
        // get server state information
        sendMessage("GETS All");
        String[] receivedMsgs = readMessage().split(" ");
        int serverCount = atoi(receivedMsgs[DATA.nRecs.ordinal()]);

        sendMessage("OK");

        // gotta pick the server that comes first with the largest number of cores
        // e.g.
        /* name     core   memory
         * medium    4      100
         * large     4      200
         */
        // gotta pick medium not large

        for (int i = 0; i < serverCount; i++) {
            receivedMsgs = readMessage().split(" ");

            String serverType = receivedMsgs[GETS.serverType.ordinal()];
            int coreCount = atoi(receivedMsgs[GETS.serverID.ordinal()]);
            int serverID = atoi(receivedMsgs[GETS.core.ordinal()]);

            if (coreCount > largestCoreCount) {
                serverList.clear();
                serverList.add(serverID);
                largestCoreCount = coreCount;
                largestServerType = serverType;
            } else if (serverType.equals(largestServerType)) {
                serverList.add(serverID);
            }
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

        mainLoop: while (true) {
            String[] receivedMsgs = readMessage().split(" ");
            String command = receivedMsgs[0];
            if (largestServerType == null) {
                getLargestServer();
            }
            switch (command) {
                case "NONE":
                    break mainLoop;
                case "OK":
                    sendMessage("REDY");
                    break;
                case "JOBN":
                    int jobID = atoi(receivedMsgs[JOBN.jobID.ordinal()]);
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
        }
        quitProgram();
    }
}