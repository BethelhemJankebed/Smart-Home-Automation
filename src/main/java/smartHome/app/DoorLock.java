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
        }
    }
    @Override
    public void turnOff() { 
        if (state) {
            System.out.println(name + " is UNLOCKED"); 
            state = false;
        }
    }
}