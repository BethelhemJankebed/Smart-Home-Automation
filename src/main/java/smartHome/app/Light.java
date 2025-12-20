package smartHome;

public class Light extends Device {

    private boolean linkable = false;
    private boolean isOn = false;
    private Camera linkedCamera;

    public Light(String name) {
        super(name);
    }

    @Override
    public void turnOn() {
        if (!isOn) {
            System.out.println(name + " is ON");
            isOn = true;
        }
    }

    @Override
    public void turnOff() {
        if (isOn) {
            System.out.println(name + " is OFF");
            isOn = false;
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
        cam.linkLight(this);
    }

    public void unlinkCamera() {
        if (linkedCamera != null) {
            linkedCamera.unlinkLight(this);
            linkedCamera = null;
            System.out.println(name + " unlinked from camera");
        }
    }
}
