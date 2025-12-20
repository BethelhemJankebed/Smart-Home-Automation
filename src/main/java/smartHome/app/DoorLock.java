package smartHome;

public class DoorLock extends Device {
    public DoorLock(String name) { super(name); }

    @Override
    public void turnOn() { System.out.println(name + " is LOCKED"); }
    @Override
    public void turnOff() { System.out.println(name + " is UNLOCKED"); }
}