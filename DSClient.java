import java.net.Socket;
import java.io.*;

public class DSClient {
    static final String[] schedAlgs = { "lrr" };
    static final int MAX_PORT_NUM = 65535;
    static final int MIN_PORT_NUM = 49152;
    static final String MAX_PORT_NUM_STRING = String.valueOf(MAX_PORT_NUM);
    static final String MIN_PORT_NUM_STRING = String.valueOf(MIN_PORT_NUM);
    static final String PORT_RANGE_STRING = "{" + MIN_PORT_NUM_STRING + " ~ " + MAX_PORT_NUM_STRING + "}";
    static final String AUTH_INFO = "isac";

    static String schedAlg = "lrr";
    static boolean verbose = false;
    static int portNum = 50000;

    static void showHelp() {
        System.out.println("Usage:");
        System.out.println("            java DSClient.java [-h] [-v] [-p " + PORT_RANGE_STRING + "] -a lrr");
    }

    static void notifyArgRequired(String arg) {
        System.out.println("Option " + arg + " requires an argument");
        showHelp();
    }

    static void notifyInvalidAlg(String param) {
        System.out.println("Err: invalid algorithm (" + param + ")!");
        showHelp();
    }

    static void notifyPortOutOfRange(String param) {
        System.out.println(param + ": Out of TCP/IP port range! " + PORT_RANGE_STRING);
        showHelp();
    }

    static void notifyInvalidNumFormat(String param) {
        System.out.println(param + ": Invalid number format!");
        showHelp();
    }

    static Socket socket;
    static DataInputStream inStream;
    static DataOutputStream outStream;
    static BufferedReader bufferedReader;

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
        /*
         * options:
         * -h: show help
         * -v: verbose show detailed information during simulation
         * -a: select algorithm
         * -p: select port number
         */

        // if there is no arguments then exit
        if (args.length == 0) {
            showHelp();
            return;
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg)
            {
                case "-h":
                    showHelp();
                    return;
                case "-v":
                    // TODO
                    break;
                default:
                    i++;
                    // if this argument is the last argument then exit
                    if (i == args.length) {
                        notifyArgRequired(arg);
                        return;
                    }
                    String param = args[i];
                    switch (arg) {
                        case "-a":
                            // check if the given algorithm is a valid algorithm
                            boolean isValid = false;
                            for (int j = 0; j < schedAlgs.length; j++) {
                                if (schedAlgs[j].equals(param)) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (!isValid) {
                                notifyInvalidAlg(param);
                                return;
                            }
                            break;
                        case "-p":
                            // first check port range by string length
                            if (param.length() < MIN_PORT_NUM_STRING.length()
                                    || param.length() > MAX_PORT_NUM_STRING.length()) {
                                notifyPortOutOfRange(param);
                                return;
                            } else {
                                try {
                                    portNum = Integer.parseInt(param);
                                } catch (NumberFormatException e) {
                                    notifyInvalidNumFormat(param);
                                    return;
                                }
                                // second check port range after parsing the parameter to int
                                if (portNum < MIN_PORT_NUM || portNum > MAX_PORT_NUM) {
                                    notifyPortOutOfRange(param);
                                    return;
                                }
                            }
                            break;
                    }
            }
        }

        socket = new Socket("localhost", portNum);
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        String inString = "";

        // HELO
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

        outStream.close();
        socket.close();
    }
}