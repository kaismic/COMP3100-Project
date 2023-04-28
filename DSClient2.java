import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class DSClient2 {
    enum JOBN {
        command, submitTime, jobID, estRunTime, core, memory, disk;
    }

    enum DATA {
        command, nRecs, recLen;
    }

    enum GETS {
        serverType, serverID, state, curStartTime, core, memory, disk, waitingJobNum, runningJobNum;
    }

    enum ALG {
        LRR, FC
    }

    static final String IP_ADDRESS = "localhost";
    static final String AUTH_INFO = "isac";

    static final int portNum = 50000;

    static Socket socket;
    static BufferedReader reader;
    static DataOutputStream outStream;

    static ALG currAlg = ALG.LRR;

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

    static void quit() throws IOException {
        sendMessage("QUIT");
        readMessage();

        reader.close();
        outStream.close();
        socket.close();
    }

    static void getLargestServer() throws IOException {
        // get server state information
        sendMessage("GETS All");
        String[] receivedMsg = readMessage().split(" ");
        int serverCount = atoi(receivedMsg[DATA.nRecs.ordinal()]);

        sendMessage("OK");

        // gotta pick the server that comes first with the largest number of cores
        // e.g.
        /* name     core   memory
         * medium    4      100
         * large     4      200
         */
        // gotta pick medium not large

        for (int i = 0; i < serverCount; i++) {
            receivedMsg = readMessage().split(" ");

            String serverType = receivedMsg[GETS.serverType.ordinal()];
            int coreCount = atoi(receivedMsg[GETS.core.ordinal()]);
            int serverID = atoi(receivedMsg[GETS.serverID.ordinal()]);

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

    static String getFirstCapable() throws IOException {
        // get server state information
        sendMessage("GETS Capable");
        String[] dataMsg = readMessage().split(" ");
        int serverCount = atoi(dataMsg[DATA.nRecs.ordinal()]);

        sendMessage("OK");

        String firstCapable = "";

        for (int i = 0; i < serverCount; i++) {
            if (firstCapable.isEmpty()) {
                firstCapable = readMessage();
            } else {
                readMessage();
            }
        }
        sendMessage("OK");
        readMessage(); // receive "."

        return firstCapable;
    }

    public static void main(String args[]) throws IOException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                switch (args[i].charAt(1)) {
                    case 'a':
                        i++; // next argument
                        switch (args[i].toLowerCase(Locale.ENGLISH)) {
                            case "lrr":
                                currAlg = ALG.LRR;
                                break;
                            case "fc":
                                currAlg = ALG.FC;
                                break;
                            default:
                                System.out.println("Unsupported algorithm");
                                return;
                        }
                }
            } else {
                System.out.println("Invalid argument");
            }
        }

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

        switch (currAlg) {
            case LRR:
                break;
            case FC:
            fcLoop: while (true) {
                String[] receivedMsgs = readMessage().split(" ");
                String command = receivedMsgs[0];

                switch (command) {
                    case "NONE":
                        break fcLoop;
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
                    //     break fcLoop;
                    // case "JOBP":
                    //     break fcLoop;
                    // case "RESF":
                    //     break fcLoop;
                    // case "RESR":
                    //     break fcLoop;
                    // case "CHKQ":
                    //     break fcLoop;
                    default:
                        break fcLoop;
                }
            }
            default:
                break;
            
        }



        quit();
    }
}
