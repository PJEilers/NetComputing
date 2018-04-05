package Backend;

import java.io.*;
import java.net.*;
import java.lang.*;

//Sensor of 1 parking spot associated with an areaserver
public class Sensor extends Thread implements Serializable{
    private SensorPackage pack;
    private String ip;
    private int port;

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
    public int getPort(){
        return this.port;
    }
    public String getIp(){
        return this.ip;
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
            //Connection to corresponding areaserver

            java.util.Random rand = new java.util.Random();
            while (true) {
                double r = rand.nextDouble();
                //Simulate coming and going of cars
                if (r < 0.05) {
                    s = new Socket(ip, port);
                    in = new ObjectInputStream(s.getInputStream());
                    out = new ObjectOutputStream(s.getOutputStream());

                    out.flush();
                    if(this.pack.isAvailable()==true) {
                        this.pack.setAvailable(false);
                    } else {
                        this.pack.setAvailable(true);
                    }
                    out.writeObject(pack);
                    out.reset();
                    s.close();
                    in.close();
                    out.close();
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
