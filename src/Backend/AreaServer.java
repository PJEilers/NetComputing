package Backend;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;


public class AreaServer extends Thread {
    private int port;
    private String ip;
    private ArrayList<Sensor> sensors;
    private ArrayList<Sensor> backup;
    private int index;
    private final static String QUEUE_NAME_ONE = "backups";
    private int[] closeAreas;
    private String recQueue, sendQueue;
    private  ServerSocket serverSocket;
    private BackupListener b;
    private ReqThread r;


    AreaServer(int port, int num, String ip) {
        this.ip=ip;
        this.port = port;
        this.index = num;
        this.sensors = new ArrayList<Sensor>(Database.getSENSORS().get(index));
        this.recQueue = "backup".concat(Integer.toString(index));
        System.out.println("area " + port);

    }

    //Called before shutting down
    public void closeAll() {
        try {
            serverSocket.close();
            b.interrupt();
            r.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getPort(){
        return this.port;
    }

    public String getIp() {
        return ip;
    }

    //Send sensors over rabbit mq as backup
    public void sendBackup(){
        try {
            //Send it to the correct receiver, the next area currently running
            this.sendQueue = "backup".concat(Integer.toString((index+1)%Database.getARLEN()));
            ConnectionFactory factory = new ConnectionFactory();

            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(sendQueue, false, false, false, null);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            byte[] yourBytes;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(sensors);
                out.flush();
                yourBytes = bos.toByteArray();

            } finally {
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
            channel.basicPublish("", sendQueue, null, yourBytes);
            channel.close();
            connection.close();
        } catch (TimeoutException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    //Called if its the requested area, it checks neighbouring areas if full in order of proximity
    public SensorPackage getFreeSpot() {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).isAvailable() == true) {

                return sensors.get(i).getPack();
            }
        }
        this.closeAreas= Database.getClosest(index);
        //NO FREE SPOT IN AREA, find closest peers and request free spot there in order
        try {
            for (int i = 0; i < closeAreas.length; i++) {
                Socket s = null;
                ObjectOutputStream out;
                ObjectInputStream in;
                int port = Database.getAREAS().get(closeAreas[i]).getPort();
                String ip = Database.getAREAS().get(closeAreas[i]).getIp();
                s = new Socket(ip, port);

                in = new ObjectInputStream(s.getInputStream());
                out = new ObjectOutputStream(s.getOutputStream());

                out.flush();
                //To prevent infinite loop
                out.writeObject("nocheck");


                String str = (String) in.readObject();

                //Spot found!
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
        //Everything is full!
        return new SensorPackage("NO SPACE AVAILABLE ANYWHERE", 0, true);
    }

    //Called if not the requested area
    public SensorPackage getFreeSpotNoCheck() {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).isAvailable() == true) {
                return sensors.get(i).getPack();
            }
        }
        return new SensorPackage("Fail", 0, true);
    }

    //Called when there is a preference for a street in a given area, first check that street then continue in the area
    public SensorPackage getFreeSpot(String streetname) {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).isAvailable() == true&&sensors.get(i).getStreetname().equals(streetname)) {
                return sensors.get(i).getPack();
            }
        }
        return getFreeSpot();
    }

    public void run() {
        try {
            this.serverSocket = new ServerSocket(port);

            //Start backup listener
            b = new BackupListener();

            //Receive sensor data and parking requests
            while (true) {
                Socket clientSocket = serverSocket.accept();

                //start a thread that waits for a client message coming in
                r = new ReqThread(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Handle find free spot request and sensor data
    class ReqThread extends Thread {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket clientSocket;

        //Establish connection and start
        public ReqThread(Socket aSocket) {
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
                dataGeneral = in.readObject();
                if(dataGeneral instanceof  String){
                    //The data is a find spot request, check if you are the begin area or not and handle accordingly
                    String data2 = (String) dataGeneral;
                    if(data2.equals("nocheck")) {
                        SensorPackage result = getFreeSpotNoCheck();
                        if (result.getStreetName().equals("Fail")) {
                            out.writeObject("Fail");
                        } else {
                            out.writeObject(result.getStreetName() + " " + result.getSpot());
                        }
                    } else {
                        SensorPackage result = getFreeSpot();
                        out.writeObject(result.getStreetName() + " " + result.getSpot());
                    }

                } else {
                    //The data received is the update of the state of a sensor, change accordingly
                    SensorPackage data = (SensorPackage) dataGeneral;
                    for (int i = 0; i < sensors.size(); i++) {
                        if (data.getStreetName().equals(sensors.get(i).getStreetname()) && data.getSpot() == sensors.get(i).getSpot()) {
                            sensors.get(i).setPack(data);
                        }
                    }
                    sendBackup();
                    System.out.println(data.getStreetName() + " at pos " + data.getSpot() + " now is " + data.isAvailable());

                }


            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    //Receives and stores backups
    class BackupListener extends Thread {
        public BackupListener() {
            this.start();

        }

        public void run() {
            try {
                //Start the rabbitmq server
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(recQueue, false, false, false, null);

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        //Backup sensor data is received, update
                        ByteArrayInputStream bis = new ByteArrayInputStream(body);
                        ObjectInput in = null;
                        try {
                            in = new ObjectInputStream(bis);
                            ArrayList<Sensor> o = (ArrayList<Sensor>) in.readObject();
                            backup = new ArrayList<Sensor>(o);
                            System.out.println("changed");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (in != null) {
                                    in.close();
                                }
                            } catch (IOException ex) {
                                // ignore close exception
                            }
                        }

                    }
                };
                channel.basicConsume(recQueue, true, consumer);
            } catch (IOException e){
                e.printStackTrace();
            } catch (TimeoutException e){
                e.printStackTrace();
            }
        }
    }

}
