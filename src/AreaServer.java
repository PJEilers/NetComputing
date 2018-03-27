import java.io.*;
import java.net.*;

public class AreaServer extends Thread{
    private int port;
    AreaServer(int port){
        this.port=port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                //start a thread that waits for a client message coming in
                new ConnectionThread(clientSocket);
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    class ConnectionThread extends Thread {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket clientSocket;

        public ConnectionThread(Socket aSocket) {
            try {
                clientSocket = aSocket;

                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(clientSocket.getInputStream());
                this.start();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void run() {
            try {
                SensorPackage data;
                while(true) {
                    data = (SensorPackage) in.readObject();

                    System.out.println(data.getStreetName() + " at pos " + data.getSpot() +" now is " + data.isAvailable());
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch ( ClassNotFoundException e){
                e.printStackTrace();
            }finally {
                try {
                    clientSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
