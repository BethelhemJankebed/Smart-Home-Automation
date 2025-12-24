package smartHome.app;

import smartHome.db.DatabaseManager;
import java.util.List;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class TestDriver {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("====================================");
        System.out.println("   SMART HOME SYSTEM - CLI RUNNER   ");
        System.out.println("====================================");

        DatabaseManager.logSystemActivity("CLI", "Terminal System Started", "INFO");

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Manage Rooms & Devices");
            System.out.println("2. Simulate Security Events");
            System.out.println("3. View System Logs (Reporting)");
            System.out.println("4. Cleanup Database (Maintenance)");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> manageRoomsAndDevices(sc);
                case "2" -> simulateEvents(sc);
                case "3" -> viewReports(sc);
                case "4" -> maintenanceMenu(sc);
                case "5" -> {
                    System.out.println("Exiting System...");
                    DatabaseManager.logSystemActivity("CLI", "Terminal System Terminated", "INFO");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void manageRoomsAndDevices(Scanner sc) {
        System.out.println("\n--- Manage Rooms & Devices ---");
        List<Room> rooms = DatabaseManager.getAllRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms found. Let's add one.");
            addRoom(sc);
            return;
        }

        for (int i = 0; i < rooms.size(); i++) {
            System.out.println((i + 1) + ". " + rooms.get(i).getName());
        }
        System.out.println((rooms.size() + 1) + ". Add New Room");
        System.out.println((rooms.size() + 2) + ". Back to Main Menu");
        System.out.print("Choice: ");
        int choice = Integer.parseInt(sc.nextLine());

        if (choice == rooms.size() + 1) {
            addRoom(sc);
        } else if (choice <= rooms.size() && choice > 0) {
            manageDevicesInRoom(sc, rooms.get(choice - 1));
        }
    }

    private static void addRoom(Scanner sc) {
        System.out.print("Enter room name: ");
        String name = sc.nextLine();
        int id = DatabaseManager.insertRoom(name, "room_default");
        if (id != -1) {
            System.out.println("Room '" + name + "' added with ID " + id);
            DatabaseManager.logSystemActivity("App", "Room added: " + name, "INFO");
        }
    }

    private static void manageDevicesInRoom(Scanner sc, Room room) {
        System.out.println("\n--- Room: " + room.getName() + " ---");
        List<Device> devices = DatabaseManager.getDevicesForRoom(room.getId());
        if (devices.isEmpty()) {
            System.out.println("No devices in this room.");
        } else {
            for (int i = 0; i < devices.size(); i++) {
                Device d = devices.get(i);
                System.out.println((i + 1) + ". [" + (d.isOn() ? "ON" : "OFF") + "] " + d.getName() + " (" + d.getType() + ")");
            }
        }
        System.out.println((devices.size() + 1) + ". Add Device");
        System.out.println((devices.size() + 2) + ". Back");
        System.out.print("Choice: ");
        int choice = Integer.parseInt(sc.nextLine());

        if (choice == devices.size() + 1) {
            addDeviceToRoom(sc, room.getId());
        } else if (choice <= devices.size() && choice > 0) {
            Device d = devices.get(choice - 1);
            if (d.isOn()) d.turnOff(); else d.turnOn();
            DatabaseManager.updateDeviceState(d.getId(), d.isOn());
            System.out.println(d.getName() + " is now " + (d.isOn() ? "ON" : "OFF"));
            DatabaseManager.logSystemActivity("Appliances", d.getName() + " toggled " + (d.isOn() ? "ON" : "OFF"), "INFO");
        }
    }

    private static void addDeviceToRoom(Scanner sc, int roomId) {
        System.out.print("Device Type (Light/Fan/TV/Camera/DoorLock): ");
        String type = sc.nextLine();
        System.out.print("Device Name: ");
        String name = sc.nextLine();
        String id = "D" + System.currentTimeMillis() % 10000;
        
        Device d = switch(type.toLowerCase()) {
            case "light" -> new Light(id, name);
            case "fan" -> new Fan(id, name);
            case "tv" -> new TV(id, name);
            case "doorlock" -> new DoorLock(id, name);
            case "camera" -> new Camera(id, name, null);
            default -> null;
        };

        if (d != null) {
            DatabaseManager.addDevice(d, roomId);
            System.out.println(type + " added successfully.");
            DatabaseManager.logSystemActivity("App", "Device added: " + name + " to Room ID " + roomId, "INFO");
        } else {
            System.out.println("Invalid device type.");
        }
    }

    private static void simulateEvents(Scanner sc) {
        System.out.println("\n--- Simulate Events ---");
        System.out.println("1. Security: Unknown Person Detected");
        System.out.println("2. Security: Motion in Living Room");
        System.out.println("3. Security: Glass Break Detected");
        System.out.println("4. Appliances: High Energy Usage Warning");
        System.out.println("5. Back");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> DatabaseManager.logSystemActivity("Security", "Unknown Person detected at Front Door", "CRITICAL");
            case "2" -> DatabaseManager.logSystemActivity("Security", "Motion detected in Living Room", "WARNING");
            case "3" -> DatabaseManager.logSystemActivity("Security", "Possible Glass Break detected", "CRITICAL");
            case "4" -> DatabaseManager.logSystemActivity("Appliances", "High energy usage on HVAC system", "WARNING");
            default -> { return; }
        }
        System.out.println("Event simulated and logged.");
    }

    private static void viewReports(Scanner sc) {
        System.out.println("\n--- System Reports & Logs ---");
        System.out.println("Displaying the last 10 system logs:");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM events WHERE module IS NOT NULL ORDER BY id DESC LIMIT 10")) {
            
            System.out.println("TIME | MODULE | EVENT | LEVEL");
            System.out.println("------------------------------------");
            while (rs.next()) {
                String fullTs = rs.getString("timestamp");
                String ts = (fullTs != null && fullTs.length() >= 16) ? fullTs.substring(11, 16) : "??:??";
                System.out.printf("%s | %s | %s | %s\n", 
                    ts, rs.getString("module"), rs.getString("details"), rs.getString("level"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("\nPress Enter to return...");
        sc.nextLine();
    }

    private static void maintenanceMenu(Scanner sc) {
        System.out.println("\n--- Maintenance ---");
        System.out.println("1. Clear All Logs");
        System.out.println("2. Reset Database (CAUTION)");
        System.out.println("3. Back");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        if (choice.equals("1")) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM events WHERE module IS NOT NULL;");
                System.out.println("Module logs cleared.");
                DatabaseManager.logSystemActivity("Maintenance", "Logs cleared by user", "INFO");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
