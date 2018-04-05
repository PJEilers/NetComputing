package Backend;

import java.io.Serializable;

//Class used to send the information about 1 sensor to its corresponding areaserver
public class SensorPackage implements Serializable {
    private static final long serialVersionUID = -5399605122490343339L;
    private String streetName;
    private int spot;
    private boolean available;

    public SensorPackage(){
    }

    public SensorPackage(String name, int spot, boolean available) {
        this.streetName=name;
        this.spot=spot;
        this.available=available;
    }
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public int getSpot() {
        return spot;
    }

    public void setSpot(int spot) {
        this.spot = spot;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }




}
