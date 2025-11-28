package smartHome;

public class Light extends Device {
    public Light(String name) { super(name); }

    @Override
    public void turnOn() { System.out.println(name + " is ON"); }
    @Override
    public void turnOff() { System.out.println(name + " is OFF"); }
}