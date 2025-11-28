package smartHome;

import java.util.ArrayList;
import java.util.Scanner;

public class ApplicationModule {
    private Scanner sc = new Scanner(System.in);
    private ArrayList<Room> rooms = new ArrayList<>();

    public void addRoom() {
        System.out.print("Enter room name: ");
        String roomName = sc.nextLine();
        rooms.add(new Room(roomName));
        System.out.println("Room added!");
    }

    public void addDevice() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms exist. Add a room first!");
            return;
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

        switch(type.toLowerCase()) {
            case "light": device = new Light("Light"); break;
            case "fan": device = new Fan("Fan"); break;
            case "doorlock": device = new DoorLock("DoorLock"); break;
            case "camera": device = new Camera("Camera"); break;
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
        System.out.print("Choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) device.turnOn();
        else if (choice == 2) device.turnOff();
        else System.out.println("Invalid choice!");
    }
}