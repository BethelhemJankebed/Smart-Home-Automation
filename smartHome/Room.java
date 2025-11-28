package smartHome;

import java.util.ArrayList;

public class Room {
    private String name;
    private ArrayList<Device> devices = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public void showDevices() {
        if (devices.isEmpty()) {
            System.out.println("   No devices in this room.");
        } else {
            for (int i = 0; i < devices.size(); i++) {
                System.out.println("   " + (i+1) + ". " + devices.get(i).getName());
            }
        }
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }
}