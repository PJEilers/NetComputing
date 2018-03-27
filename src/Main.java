public class Main {
    private static final AreaServer[] AREAS = {new AreaServer(7000),new AreaServer(7001),new AreaServer(7002)};
    private static final Sensor[][] SENSORS = {{new Sensor("localhost",7000, "A-weg", 0),new Sensor("localhost",7000,"A-weg", 1)},
            {new Sensor("localhost",7001,"B-weg", 0),new Sensor("localhost",7001,"B-weg", 1)},
            {new Sensor("localhost",7002,"C-weg", 0)}};

    public static void main(String[] args){

        for(int i = 0;i<AREAS.length;i++){
            AreaServer server = AREAS[i];
            server.start();
            for (int j = 0; j < SENSORS[i].length; j++) {
                Sensor s = SENSORS[i][j];
                s.start();
            }
        }


        return;
    }

}
