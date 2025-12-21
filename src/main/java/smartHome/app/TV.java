package smartHome.app;



public class TV extends Device {
    public TV(String id, String name) {
        super(id, name, "TV");
    }

    @Override
    public void turnOn() {
        if (!state) {
            System.out.println(name + " is ON");
            state = true;
        }
    }

    @Override
    public void turnOff() {
        if (state) {
            System.out.println(name + " is OFF");
            state = false;
        }
    }
}
