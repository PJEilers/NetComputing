package Client;

import Backend.SensorPackage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

//Used instead of the web service to find a parking spot in a given area
public class Client {
    public static void main(String[] args) {
        int i=0;
        int spot=-1;
        SensorPackage sp = null;
        Scanner reader = null;
        //Surrogate restfull webservice
        try {
            while (true) {
                reader = new Scanner(System.in);
                System.out.println("Enter the number of the area you want a parking spot in: ");
                int n = reader.nextInt();
                Socket s = null;
                ObjectOutputStream out;
                ObjectInputStream in;

                //connect to the proper area
                s = new Socket("localhost", 7000 + n);

                in = new ObjectInputStream(s.getInputStream());
                out = new ObjectOutputStream(s.getOutputStream());

                out.flush();
                out.writeObject("check");


                String str = (String) in.readObject();

                if (!str.equals("Fail")) {
                    String[] split = str.split(" ");
                    sp= new SensorPackage(split[0], Integer.parseInt(split[1]), true);
                    System.out.println("request in area " + n + " now gives : " + sp.getStreetName() + " " + sp.getSpot());
                } else {
                    System.out.println("No available spot found!");
                }
                out.close();
                in.close();
                s.close();

                sp = null;
                //To prevent unreachable code
                if (i == 10)
                    break;

            }
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        reader.close();
        return;
    }
}
