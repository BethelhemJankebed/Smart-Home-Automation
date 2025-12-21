package smartHome.app;

import java.util.ArrayList;
import java.util.Scanner;

public class ApplicationModule {
    private final Scanner sc = new Scanner(System.in);
    private ArrayList<Room> rooms = new ArrayList<>();
    private int roomIdCounter = 1;
    private int deviceIdCounter = 1;

    public void addRoom() {
        System.out.print("Enter room name: ");
        String roomName = sc.nextLine();
        rooms.add(new Room(roomIdCounter++, roomName));
        System.out.println("Room added!");
    }

    public void addDevice() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms exist. Add a room first!");
            return;
        }

        System.out.println("===== Rooms =====");
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println((i + 1) + ". " + rooms.get(i).getName());
        }

        System.out.print("Enter room number (starting from 1): ");
        int roomIndex = sc.nextInt() - 1;
        sc.nextLine();

        if (roomIndex < 0 || roomIndex >= rooms.size()) {
            System.out.println("Invalid room number!");
            return;
        }

        Room selectedRoom = rooms.get(roomIndex);

        System.out.print("Enter device type (Light/Fan/DoorLock/Camera): ");
        String type = sc.nextLine();
        Device device = null;

        String id = "D" + deviceIdCounter++;
        switch(type.toLowerCase()) {
            case "light": device = new Light(id, "Light"); break;
            case "fan": device = new Fan(id, "Fan"); break;
            case "doorlock": device = new DoorLock(id, "DoorLock"); break;
            case "camera": device = new Camera(id, "Camera", selectedRoom); break;
            default: System.out.println("Invalid device type!"); return;
        }

        selectedRoom.addDevice(device);
        System.out.println(type + " added to " + selectedRoom.getName());
    }

    public void showRoomsAndDevices() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms exist!");
            return;
        }

        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            System.out.println((i+1) + ". Room: " + r.getName());
            r.showDevices();
        }
    }

    public void controlDevice() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms exist!");
            return;
        }

        System.out.println("===== Rooms =====");
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println((i + 1) + ". " + rooms.get(i).getName());
        }

        System.out.print("Enter room number to control (starting from 1): ");
        int roomIndex = sc.nextInt() - 1;
        sc.nextLine();

        if (roomIndex < 0 || roomIndex >= rooms.size()) {
            System.out.println("Invalid room number!");
            return;
        }

        Room controlRoom = rooms.get(roomIndex);
        if (controlRoom.getDevices().isEmpty()) {
            System.out.println("No devices in this room!");
            return;
        }

        controlRoom.showDevices();
        System.out.print("Enter device number to control (starting from 1): ");
        int deviceIndex = sc.nextInt() - 1;
        sc.nextLine();

        if (deviceIndex < 0 || deviceIndex >= controlRoom.getDevices().size()) {
            System.out.println("Invalid device number!");
            return;
        }

        Device device = controlRoom.getDevices().get(deviceIndex);

        System.out.println("1. Turn ON");
        System.out.println("2. Turn OFF");

        if (device instanceof Light light) {

            if (!light.isLinkable()) {
                System.out.println("3. Make linkable");
            }
            else {
                if (light.isLinked()) {
                    System.out.println("3. Unlink from camera");
                } else {
                    System.out.println("3. Link with camera");
                }
                System.out.println("4. Make unlinkable");
            }
        }

        System.out.print("Choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

// ---- Actions ----
        if (choice == 1) {
            device.turnOn();
        }
        else if (choice == 2) {
            device.turnOff();
        }
        else if (device instanceof Light light) {

            if (choice == 3) {
                if (!light.isLinkable()) {
                    light.makeLinkable();
                    System.out.println(light.getName() + " is now linkable.");
                }
                else if (light.isLinked()) {
                    light.unlinkCamera();
                }
                else {
                    Camera cam = controlRoom.chooseCamera(sc);
                    if (cam != null) {
                        light.linkCamera(cam);
                        System.out.println(light.getName() + " linked with " + cam.getName());
                    }
                }
            }

            else if (choice == 4 && light.isLinkable()) {
                light.makeUnlinkable();
                System.out.println(light.getName() + " is now unlinkable.");
            }

            else {
                System.out.println("Invalid choice!");
            }
        }
    }
}