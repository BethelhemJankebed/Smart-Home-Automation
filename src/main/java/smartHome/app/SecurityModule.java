package smartHome.app;

import org.opencv.core.Mat;

public class SecurityModule {
    public void run() {
        System.out.println("=== Security Module ===");
        System.out.println("Simulating door locks and cameras...");
        // Future: add room/device selection and snapshot
    }

    public static void receiveImgWarning(Mat image, String warning){
        //gets frame
    }
}