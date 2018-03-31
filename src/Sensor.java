import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Sensor extends Thread {
    private SensorPackage pack;
    private String ip;
    private int port;
    private final static String QUEUE_NAME = "hello";
    Sensor(String ip, int port, String name, int spot) {
        this.ip = ip;
        this.port = port;
        this.pack = new SensorPackage(name,spot,true);
    }
    public boolean isAvailable(){
        return this.pack.isAvailable();
    }
    public String getStreetname(){
        return this.pack.getStreetName();
    }
    public int getSpot(){
        return this.pack.getSpot();
    }

    public SensorPackage getPack() {
        return pack;
    }
    public void setPack(SensorPackage sp){
        this.pack = sp;
    }

    public void run() {
        Socket s = null;
        ObjectOutputStream out;
        ObjectInputStream in;

        try {
            s = new Socket(ip, port);

            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());

            out.flush();
            java.util.Random rand = new java.util.Random();
            while (true) {
                double r = rand.nextDouble();
                if (r < 0.1) {
                    if(this.pack.isAvailable()==true) {
                        this.pack.setAvailable(false);
                    } else {
                        this.pack.setAvailable(true);
                    }

                    out.writeObject(pack);
                    out.reset();/*
                    try {


                        ConnectionFactory factory = new ConnectionFactory();
                        factory.setHost("localhost");
                        Connection connection = factory.newConnection();
                        // Next we create a channel, which is where most of the API for getting
                        // things done resides.
                        Channel channel = connection.createChannel();
                        // To send, we must declare a queue for us to send to; then we can
                        // publish a message to the queue:
                        // Declaring a queue is idempotent - it will only be created if it
                        // doesn't exist already. The message content is a byte array, so you
                        // can encode whatever you like there.
                        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                        String message = "Hello World!";
                        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                        System.out.println(" [x] Sent '" + message + "'");
                        //Lastly, we close the channel and the connection
                        channel.close();
                        connection.close();
                    } catch (TimeoutException e){
                        e.printStackTrace();
                    }*/
                }
                synchronized (this){
                    this.wait(1000);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);

        } catch (InterruptedException e){
           System.err.println(e);
        }


    }
}
