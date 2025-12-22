package smartHome.app;

public class Light extends Device {

    private boolean linkable = false;
    private Camera linkedCamera;

    public Light(String id, String name) {
        super(id, name, "Light");
    }

    @Override
    public void turnOn() {
        if (!state) {
            System.out.println(name + " is ON");
            state = true;
            smartHome.db.DatabaseManager.updateDeviceState(id, true);
        }
    }

    @Override
    public void turnOff() {
        if (state) {
            System.out.println(name + " is OFF");
            state = false;
            smartHome.db.DatabaseManager.updateDeviceState(id, false);
        }
    }

    // ---- Linking logic ----
    public boolean isLinkable() {
        return linkable;
    }

    public boolean isLinked() {
        return linkedCamera != null;
    }

    public void makeLinkable() {
        linkable = true;
    }

    public void makeUnlinkable() {
        unlinkCamera();
        linkable = false;
    }

    public void linkCamera(Camera cam) {
        if (!linkable || cam == null) return;
        linkedCamera = cam;
    }

    public void unlinkCamera() {
        if (linkedCamera != null) {
            linkedCamera = null;
            System.out.println(name + " unlinked from camera");
        }
    }
}
