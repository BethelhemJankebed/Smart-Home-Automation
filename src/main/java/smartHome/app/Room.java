package smartHome.app;



import java.util.ArrayList;
import java.util.Scanner;

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
        this(id, name, "🏠"); // Default icon
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

    public void showDevices() {
        if (devices.isEmpty()) {
            System.out.println("   No devices in this room.");
        } else {
            for (int i = 0; i < devices.size(); i++) {
                System.out.println("   " + (i+1) + ". " + devices.get(i).getName());
            }
        }
    }

    public Camera chooseCamera(Scanner sc) {
        ArrayList<Camera> cameras = new ArrayList<>();

        for (Device d : devices) {
            if (d instanceof Camera cam) {
                cameras.add(cam);
            }
        }

        if (cameras.isEmpty()) {
            System.out.println("No cameras available in this room!");
            return null;
        }

        System.out.println("Select a camera:");
        for (int i = 0; i < cameras.size(); i++) {
            System.out.println((i + 1) + ". " + cameras.get(i).getName());
        }

        System.out.print("Camera number: ");
        int choice = sc.nextInt() - 1;
        sc.nextLine();

        if (choice < 0 || choice >= cameras.size()) {
            System.out.println("Invalid camera selection!");
            return null;
        }

        return cameras.get(choice);
    }


    public ArrayList<Device> getDevices() {
        return devices;
    }
}