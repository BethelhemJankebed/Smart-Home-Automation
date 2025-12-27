package smartHome.app;

import java.util.ArrayList;

public class Room {
    private int id;
    private String name;
    private String icon;
    private ArrayList<Device> devices = new ArrayList<>();

    public Room(int id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public Room(int id, String name) {
        this(id, name, "🏠"); 
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addDevice(Device device) {
        devices.add(device);
    }




    public ArrayList<Device> getDevices() {
        return devices;
    }
}