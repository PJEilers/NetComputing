import java.io.*;
import java.net.*;
import java.sql.Time;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.QueueingConsumer;


public class AreaServer extends Thread{
    private int port;
    private Sensor[] sensors;
    private int index;
    private final static String QUEUE_NAME = "hello";
    private static final Sensor[][] SENSORS = {{new Sensor("localhost",7000, "A-weg", 0),new Sensor("localhost",7000,"A-weg", 1)},
            {new Sensor("localhost",7001,"B-weg", 0),new Sensor("localhost",7001,"B-weg", 1)},
            {new Sensor("localhost",7002,"C-weg", 0)}};
    AreaServer(int port, int num){
        this.port=port;
        this.index = num;
        this.sensors = SENSORS[index];
    }
    public SensorPackage getFreeSpot(){
        for (int i = 0; i < sensors.length; i++) {
            if (sensors[i].isAvailable()==true){

                return sensors[i].getPack();
            }
        }
        return new SensorPackage();
    }
    public void getFreeSpot(String streetName){


    }
    public void run() {
        try {
            /*ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(QUEUE_NAME, true, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                System.out.println(" [x] Received '" + message + "'");
            }*/
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
        }*/ catch (IOException e){
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
                SensorPackage data;
                while(true) {
                    data = (SensorPackage) in.readObject();
                    for (int i = 0; i < sensors.length; i++) {
                        if(data.getStreetName().equals(sensors[i].getStreetname())&&data.getSpot()==sensors[i].getSpot()){

                            sensors[i].setPack(data);
                        }
                    }
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
