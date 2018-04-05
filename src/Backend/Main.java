package Backend;


import java.util.Scanner;

public class Main{

    public static void main(String[] args) {
        Database d = new Database();

        //Start areas and their sensors
        for(int i = 0;i<Database.getAREAS().size();i++){
            AreaServer server = Database.getAREAS().get(i);
            server.start();
            for (int j = 0; j < Database.getSENSORS().get(i).size(); j++) {
                Sensor s = Database.getSENSORS().get(i).get(j);
                s.start();
            }
        }

        Scanner reader;
        int j=0;
        int n =Database.getAREAS().size();
        while(true){
            reader = new Scanner(System.in);
            System.out.println("Enter a number to activate a new area with that many sensors (current " + n + "): " );
            n = reader.nextInt();

            d.addArea(n);
            n++;

            //prevent unreachable code of reader.close
            if(j==10)
                break;
        }

        //Clean up when this is somehow reached
        for (j = 0; j < Database.getAREAS().size(); j++) {
            for (int i = 0; i < Database.getSENSORS().get(j).size(); i++) {
                Database.getSENSORS().get(j).get(i).interrupt();

            }
            Database.getAREAS().get(j).closeAll();
            Database.getAREAS().get(j).interrupt();
        }
        reader.close();
        return;
    }

}
