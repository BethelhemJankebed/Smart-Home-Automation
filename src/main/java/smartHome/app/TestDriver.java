package smartHome.app;

import smartHome.db.DatabaseManager;
import java.util.List;

public class TestDriver {
    public static void main(String[] args) {
        System.out.println("--- Starting Test Driver ---");

        // 1. Create Room
        System.out.println("Creating Room...");
        Room livingRoom = new Room(1, "Living Room");
        
        // 2. Create Devices
        System.out.println("Creating Devices...");
        Light light1 = new Light("L1", "Main Light");
        TV tv1 = new TV("T1", "Samsung TV");
        
        livingRoom.addDevice(light1);
        livingRoom.addDevice(tv1);
        
        // 3. Register Devices to DB
        System.out.println("Adding Devices to DB...");
        DatabaseManager.addDevice(light1, livingRoom.getId());
        DatabaseManager.addDevice(tv1, livingRoom.getId());

        // 4. Verify Devices from DB
        System.out.println("Verifying Devices from DB...");
        List<Device> dbDevices = DatabaseManager.getDevicesForRoom(livingRoom.getId());
        System.out.println("Devices found in DB: " + dbDevices.size());
        for(Device d : dbDevices) {
            System.out.println("  - " + d.getName() + " (" + d.getType() + ")");
        }

        // 5. Camera Setup
        System.out.println("Setting up Camera...");
        Camera cam = new Camera("C1", "Security Cam", livingRoom);
        
        // 6. Link Light to Camera (Implicitly done by being in the Linked Room and isLinkable)
        System.out.println("Linking Light to Camera...");
        light1.makeLinkable();
        light1.linkCamera(cam); // Light needs to know about camera for isLinked() check

        // 7. Simulate Motion
        System.out.println("Simulating Motion...");
        // Manual simulation of logic since we can't easily trigger OpenCV without camera
        // Using internal logic inspection:
        if (livingRoom.getDevices().contains(light1)) {
           System.out.println("Light is in the room.");
        }
        if (light1.isLinked()) {
             System.out.println("Light is linked.");
             light1.turnOn();
        }
        
        // 8. Log Event
        System.out.println("Logging Event...");
        DatabaseManager.logEvent(cam.getId(), "MOTION", "test_snapshot.jpg");
        
        System.out.println("--- Test Driver Log Complete ---");
    }
}
