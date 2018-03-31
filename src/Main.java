import java.util.concurrent.TimeUnit;
public class Main{
    private static final AreaServer[] AREAS = {new AreaServer(7000,0,new int[]{1,2}),new AreaServer(7001,1,new int[]{2,0}),new AreaServer(7002,1,new int[]{0,1})};
    private static final Sensor[][] SENSORS = {{new Sensor("localhost",7000, "A-weg", 0),new Sensor("localhost",7000,"A-weg", 1)},
            {new Sensor("localhost",7001,"B-weg", 0),new Sensor("localhost",7001,"B-weg", 1)},
            {new Sensor("localhost",7002,"C-weg", 0)}};

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
        while(true){
            sp  = AREAS[0].getFreeSpot();
            System.out.println("request in first area now gives : " +sp.getStreetName()+ " " + sp.getSpot());
            TimeUnit.SECONDS.sleep(1);
            sp=null;
            if(i==10)
                break;
        }

        return;
    }

}
