package smartHome.app;

public class Fan extends Device {
    public Fan(String name) { super(name); }

    @Override
    public void turnOn() { System.out.println(name + " is ON"); }
    @Override
    public void turnOff() { System.out.println(name + " is OFF"); }
}