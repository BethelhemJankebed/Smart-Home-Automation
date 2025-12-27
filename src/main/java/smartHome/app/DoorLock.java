package smartHome.app;

public class DoorLock extends Device {
    public DoorLock(String id, String name) { 
        super(id, name, "DoorLock"); 
    }

    @Override
    public void turnOn() { 
        if (!state) {
            System.out.println(name + " is LOCKED"); 
            state = true;
            smartHome.db.DatabaseManager.updateDeviceState(id, true);
            fireStateChanged();
        }
    }
    @Override
    public void turnOff() { 
        if (state) {
            System.out.println(name + " is UNLOCKED"); 
            state = false;
            smartHome.db.DatabaseManager.updateDeviceState(id, false);
            fireStateChanged();
        }
    }
}