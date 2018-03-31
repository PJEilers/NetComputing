package nc;

import java.io.*;
import java.net.*;

import static nc.Constants.SENSORS;

public class AreaServer extends Thread {
    private int port;
    private Sensor[] sensors;
    private int index;
    private final static String QUEUE_NAME = "hello";

    private int[] closeAreas;

    AreaServer(int port, int num, int[] close) {
        this.port = port;
        this.index = num;
        this.closeAreas = close;
        this.sensors = Constants.SENSORS[index];
    }

    //Called if its the requested area, it checks neighbouring areas if full in order of proximity
    public SensorPackage getFreeSpot() {
        for (int i = 0; i < sensors.length; i++) {
            if (sensors[i].isAvailable() == true) {

                return sensors[i].getPack();
            }
        }
        //NO FREE SPOT IN AREA, find closest peer and request
        try {
            for (int i = 0; i < closeAreas.length; i++) {
                Socket s = null;
                ObjectOutputStream out;
                ObjectInputStream in;
                int port = Constants.SENSORS[closeAreas[i]][0].getPort();
                String ip = Constants.SENSORS[closeAreas[i]][0].getIp();
                s = new Socket(ip, port);

                in = new ObjectInputStream(s.getInputStream());
                out = new ObjectOutputStream(s.getOutputStream());

                out.flush();
                out.writeObject(new String(this.port + ""));


                String str = (String) in.readObject();

                if(!str.equals("Fail")){
                    String[] split = str.split(" ");
                    return new SensorPackage(split[0], Integer.parseInt(split[1]), true);
                }
                out.close();
                in.close();
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new SensorPackage("NOSPACEAVAILABLEANYWHERE", 0, true);
    }

    //called if not the requested area
    public SensorPackage getFreeSpotNoCheck() {
        for (int i = 0; i < sensors.length; i++) {
            if (sensors[i].isAvailable() == true) {

                return sensors[i].getPack();
            }
        }


        return new SensorPackage("Fail", 0, true);
    }

    public void getFreeSpot(String streetName) {


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
        /*catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e){
            e.printStackTrace();
        }*/ catch (IOException e) {
            e.printStackTrace();
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
                Object dataGeneral;
                while (true) {
                    dataGeneral = in.readObject();
                    if (dataGeneral instanceof SensorPackage) {
                        SensorPackage data = (SensorPackage) dataGeneral;
                        for (int i = 0; i < sensors.length; i++) {
                            if (data.getStreetName().equals(sensors[i].getStreetname()) && data.getSpot() == sensors[i].getSpot()) {

                                sensors[i].setPack(data);
                            }
                        }
                        System.out.println(data.getStreetName() + " at pos " + data.getSpot() + " now is " + data.isAvailable());

                    } else if (dataGeneral instanceof String) {
                        String data2 = (String) dataGeneral;
                        SensorPackage result = getFreeSpotNoCheck();
                        if (result.getStreetName().equals("Fail")) {
                            out.writeObject("Fail");
                            break;
                        } else {
                            out.writeObject(result.getStreetName() + " " + result.getSpot());
                            break;
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
