import java.io.*;
import java.net.*;
import java.lang.*;
public class Sensor extends Thread {
    private SensorPackage pack;
    private String ip;
    private int port;

    Sensor(String ip, int port, String name, int spot) {
        this.ip = ip;
        this.port = port;
        this.pack = new SensorPackage(name,spot,true);
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
                    out.reset();


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
