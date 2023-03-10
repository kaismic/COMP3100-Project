import java.net.*;
import java.io.*;

public class DSServer {
    public static void main(String[] args) throws Exception {
        ServerSocket ss=new ServerSocket(6561);
        Socket s=ss.accept();
        DataInputStream din=new DataInputStream(s.getInputStream());
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

        String inStr = "";
        String outStr = "";
        loop1:
        while(true){
            inStr=din.readUTF();
            System.out.println(inStr);
            switch (inStr) {
                case "HELO":
                    dout.writeUTF("G'DAY");
                    dout.flush();
                    break;
                case "BYE":
                    dout.writeUTF("BYE");
                    dout.flush();
                    break loop1;
            }

            // OutStr=br.readLine();
            // dout.writeUTF(OutStr);
            // dout.flush();
        }
        din.close();
        s.close();
        ss.close();
    }
}
