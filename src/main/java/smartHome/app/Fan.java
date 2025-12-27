package smartHome.app;

public class Fan extends Device {
    public Fan(String id, String name) { 
        super(id, name, "Fan"); 
    }

    @Override
    public void turnOn() { 
        if (!state) {
            System.out.println(name + " is SPINNING"); 
            state = true;
            smartHome.db.DatabaseManager.updateDeviceState(id, true);
            fireStateChanged();
        }
    }
    @Override
    public void turnOff() { 
        if (state) {
            System.out.println(name + " is OFF"); 
            state = false;
            smartHome.db.DatabaseManager.updateDeviceState(id, false);
            fireStateChanged();
        }
    }
}