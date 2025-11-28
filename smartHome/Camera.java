package smartHome;

public class Camera extends Device {
    public Camera(String name) { super(name); }

    @Override
    public void turnOn() { System.out.println(name + " is ACTIVE"); }
    @Override
    public void turnOff() { System.out.println(name + " is INACTIVE"); }
}