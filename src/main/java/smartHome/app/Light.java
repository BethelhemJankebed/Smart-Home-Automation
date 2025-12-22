package smartHome.app;

public class Light extends Device {

    private boolean linkable = false;
    private Camera linkedCamera;
    private String linkedCameraId;

    public Light(String id, String name) {
        super(id, name, "Light");
    }

    @Override
    public void turnOn() {
        if (!state) {
            System.out.println(name + " is ON");
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

    // ---- Linking logic ----
    public boolean isLinkable() {
        return linkable;
    }

    public boolean isLinked() {
        return linkedCameraId != null && !linkedCameraId.isEmpty();
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
        if (linkedCamera != null || linkedCameraId != null) {
            linkedCamera = null;
            linkedCameraId = null;
            System.out.println(name + " unlinked from camera");
            smartHome.db.DatabaseManager.addDevice(this, -1); // Persist change (hacky room_id needed? no, addDevice handles update)
            // Actually need room_id to update DB properly, but let the Controller handle persistence
        }
    }

    public String getLinkedCameraId() {
        return linkedCameraId;
    }

    public void setLinkedCameraId(String id) {
        this.linkedCameraId = id;
    }
}
