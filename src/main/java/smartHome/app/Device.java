package smartHome.app;

public abstract class Device {
    protected String id;
    protected String name;
    protected boolean state; // true = ON, false = OFF
    protected String type;
    private Runnable onStateChangedListener;

    public Device(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.state = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOn() {
        return state;
    }

    public String getType() {
        return type;
    }

    public abstract void turnOn();
    public abstract void turnOff();

    public void setOnStateChanged(Runnable listener) {
        this.onStateChangedListener = listener;
    }

    protected void fireStateChanged() {
        if (onStateChangedListener != null) {
            onStateChangedListener.run();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}