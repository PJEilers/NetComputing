package Backend;

import java.util.ArrayList;

//Data of all the areas and all sensors in those areas
public class Database   {
    private static ArrayList<AreaServer> AREAS;
    private  static ArrayList<ArrayList<Sensor>> SENSORS;
    private  static  int ARLEN;

    //Hardcoded sensor and area data
    public Database(){
        AREAS=new ArrayList<AreaServer>();
        SENSORS=new ArrayList<ArrayList<Sensor>>();
        SENSORS.add(new ArrayList<Sensor>());
        SENSORS.add(new ArrayList<Sensor>());
        SENSORS.add(new ArrayList<Sensor>());
        SENSORS.get(0).add(new Sensor("localhost", 7000, "A-weg 1", 0));
        SENSORS.get(0).add(new Sensor("localhost", 7000, "A-weg 1", 1));
        SENSORS.get(0).add(new Sensor("localhost", 7000, "Grote Markt", 0));

        SENSORS.get(1).add(new Sensor("localhost", 7001, "B-weg", 0));
        SENSORS.get(1).add(new Sensor("localhost", 7001, "B-weg", 1));
        SENSORS.get(1).add(new Sensor("localhost", 7001, "Zernike", 0));
        SENSORS.get(1).add(new Sensor("localhost", 7001, "Zernike", 1));

        SENSORS.get(2).add(new Sensor("localhost", 7002, "C-weg", 0));
        AREAS.add(new AreaServer(7000,0, "localhost"));
        AREAS.add(new AreaServer(7001,1,"localhost"));
        AREAS.add(new AreaServer(7002,2,"localhost"));
        ARLEN=AREAS.size();
    }

    //Add new area to program
    public void addArea(int n){
        int i = SENSORS.size();

        SENSORS.add(new ArrayList<Sensor>());
        for (int j = 0; j < n; j++) {
            SENSORS.get(i).add(new Sensor("localhost", 7000+i, (i+"-weg"), j));
        }

        AREAS.add(new AreaServer(7000+i,i,"localhost"));
        AreaServer server = Database.getAREAS().get(i);
        server.start();
        for (int j = 0; j < Database.getSENSORS().get(i).size(); j++) {
            Sensor s = Database.getSENSORS().get(i).get(j);
            s.start();
        }
    }

    //Remove area from the program
    public void remArea(int idx){
        for (int i = 0; i < SENSORS.get(idx).size(); i++) {
            SENSORS.get(idx).get(i).interrupt();

        }
        AREAS.get(idx).closeAll();
        AREAS.get(idx).interrupt();
        SENSORS.remove(idx);
        AREAS.remove(idx);
        System.out.println("REMOVED IDX FROM LIST");
    }

    public static ArrayList<AreaServer> getAREAS(){
        return AREAS;
    }
    public static ArrayList<ArrayList<Sensor>> getSENSORS(){
        return SENSORS;
    }

    public static int getARLEN() {
        return ARLEN;
    }

    //Returns the closest areas in order as an array of indexes
    public static int[] getClosest(int idx){
        int[] ret = new int[ARLEN-1];
        for (int i = 0; i < ARLEN; i++) {
            if(i!=0)ret[i-1]=(i+idx)%ARLEN;
        }
        return ret;
    }

}
