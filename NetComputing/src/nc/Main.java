package nc;
import static nc.Constants.SENSORS;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;
public class Main{
    private static final AreaServer[] AREAS = {new AreaServer(7000,0,new int[]{1,2}),new AreaServer(7001,1,new int[]{2,0}),new AreaServer(7002,2,new int[]{0,1})};


    public static void main(String[] args) throws InterruptedException {

        for(int i = 0;i<AREAS.length;i++){
            AreaServer server = AREAS[i];
            server.start();
            for (int j = 0; j < SENSORS[i].length; j++) {
                Sensor s = SENSORS[i][j];
                s.start();
            }
        }
        int i=0;
        int spot=-1;
        SensorPackage sp = null;
        Scanner reader;
        while(true){
            reader = new Scanner(System.in);  // Reading from System.in
            System.out.println("Enter a number: ");
            int n = reader.nextInt();
            sp  = AREAS[n].getFreeSpot();
            System.out.println("request in area " + n + " now gives : " +sp.getStreetName()+ " " + sp.getSpot());

            sp=null;
            if(i==10)
                break;
        }
        reader.close();
        return;
    }

}
