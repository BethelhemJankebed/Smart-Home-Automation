package smartHome;

public abstract class Device {
    protected String name;

    public Device(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void turnOn();
    public abstract void turnOff();
}