package smartHome.app;

import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import smartHome.db.DatabaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Camera extends Device {

    private volatile boolean isRunning = false;
    private VideoCapture VC;



    //models
    private final String faceModel = "src/main/resources/models/face_detection_yunet_2023mar.onnx";
    private final String recogModel = "src/main/resources/models/face_recognition_sface_2021dec.onnx";

    private final HashMap<String, Mat> familyknownEmbeddings = new HashMap<>();
    private final HashMap<String, Mat> childknownEmbeddings = new HashMap<>();
    private final HashMap<String, Long> lastAlertTime = new HashMap<>();
    private String streamSource = "0"; // Default to index 0

    private Room linkedRoom;

    public Camera(String id, String name, Room room) {
        super(id, name, "Camera");
        this.linkedRoom = room;
    }

    public String getStreamSource() { return streamSource; }
    public void setStreamSource(String source) { this.streamSource = source; }

    // ==========================================================
    // TURN CAMERA ON
    // ==========================================================
    @Override
    public void turnOn() {
        try {
            // Use OpenPNP to load the shared library automatically
            nu.pattern.OpenCV.loadShared();
            
            // Try to parse source as int, else use as string URL
            try {
                int index = Integer.parseInt(streamSource);
                VC = new VideoCapture(index, Videoio.CAP_DSHOW);
                if (!VC.isOpened()) {
                    VC = new VideoCapture(index);
                }
            } catch (NumberFormatException e) {
                // Not an integer, use as URL/file path
                VC = new VideoCapture(streamSource);
            }
            
            if (!VC.isOpened()) {
                System.out.println("Camera found but failed to open stream.");
            } else {
                // Set resolution to common standard to avoid MSMF errors
                VC.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
                VC.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
                
                // Small delay to let camera initialize
                Thread.sleep(500);
                
                System.out.println(name + " is ACTIVE");
                isRunning = true;
                loadKnownFaces();
                StartMotionDetection();
                startFaceRecognition();
            }
        } catch (Throwable e) {
            System.out.println("⚠ Failed to initialize real Camera: " + e.getMessage());
            System.out.println("Falling back to MOCK mode.");
            // e.printStackTrace(); 
        }
    }

    @Override
    public void turnOff() {
        System.out.println(name + " is INACTIVE");
        isRunning = false;
        if (VC != null && VC.isOpened()) {
            VC.release();
        }
    }

    // ==========================================================
    // LINK LIGHT FOR MOTION SENSOR MODE
    // ==========================================================



    // ==========================================================
    // LOAD KNOWN FACES
    // ==========================================================

    private void loadFacesFromFiles(File[] files, HashMap<String, Mat> knownEmbeddings){

        if (files == null || files.length == 0) {
            System.out.println("⚠ No known faces in /Faces folder");
            return;
        }

        Size size = new Size(320, 320);
        FaceDetectorYN detector = FaceDetectorYN.create(faceModel, "", size);
        detector.setInputSize(size);
        FaceRecognizerSF recognizer = FaceRecognizerSF.create(recogModel, "");

        for (File f : files) {
            Mat img = Imgcodecs.imread(f.getAbsolutePath());
            if (img.empty()) continue;

            Mat resized = new Mat();
            Imgproc.resize(img, resized, size);

            Mat detections = new Mat();
            detector.detect(resized, detections);

            if (detections.rows() == 0) continue;

            Mat aligned = new Mat();
            Mat embedding = new Mat();

            recognizer.alignCrop(resized, detections.row(0), aligned);
            recognizer.feature(aligned, embedding);

            String person = f.getName().split("\\.")[0];
            knownEmbeddings.put(person, embedding);

            System.out.println("✔ Loaded face: " + person);
        }
    }

    // MODULE WARNING SYSTEM

    private void giveImgWarningToModule(String module, Mat img){

        switch(module) {
            case "Security" -> SecurityModule.receiveImgWarning(img, "Unknown person detected on camera ''" + name + "''" );
            case "Child" -> MonitorChildModule.receiveImgWarning(img, "Baby Detected near potential sharp object on camera ''" + name + "''");
        };
    }

    public void setLinkedRoom(Room room) {
        this.linkedRoom = room;
    }

    // LOADING FACES

    private void loadKnownFaces() {

        File family_dir = new File("src/main/resources/Faces/family");
        File[] family_files = family_dir.listFiles();

        //loads family/known people
        loadFacesFromFiles(family_files, familyknownEmbeddings);

        File child_dir = new File("src/main/resources/Faces/Child");
        File[] child_files = child_dir.listFiles();

        //loads child's face
        loadFacesFromFiles(child_files, childknownEmbeddings);

    }

    private volatile Mat latestFrame;

    public Mat getLatestFrame() {
        return latestFrame;
    }

    // MOTION SENSOR THREAD + AUTO LIGHT

    private void StartMotionDetection() {
        new Thread(() -> {
            if (!VC.isOpened()) {
                System.out.println("Camera failed to open for motion detection");
                return;
            }

            Mat frame = new Mat();
            Mat gray = new Mat();
            Mat blur = new Mat();
            Mat delta = new Mat();
            Mat thresh = new Mat();
            Mat background = new Mat();

            VC.read(frame);
            Imgproc.cvtColor(frame, background, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(background, background, new Size(21, 21), 0);

            long lastMotion = System.currentTimeMillis();

            while (isRunning) {
                try {
                    synchronized (VC) {
                        if (VC == null || !VC.isOpened()) break;
                        if (!VC.read(frame) || frame.empty()) {
                            Thread.sleep(100);
                            continue;
                        }
                        latestFrame = frame.clone(); 
                    }
                } catch (Exception e) { break; }

                Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(gray, blur, new Size(21, 21), 0);

                Core.absdiff(background, blur, delta);
                Imgproc.threshold(delta, thresh, 25, 255, Imgproc.THRESH_BINARY);

                double motion = Core.countNonZero(thresh);
                background = blur.clone();

                if (motion > 5000) {
                    lastMotion = System.currentTimeMillis();
                    System.out.println("⚠ Motion detected in: " + this.name + "!");

                    if (linkedRoom != null) {
                        for (Device device : linkedRoom.getDevices()) {
                            if (device instanceof Light light && light.isLinked()) {
                                light.turnOn();
                            }
                        }
                    }
                    // Log event without snapshot for simple motion
                    DatabaseManager.logEvent(this.id, "MOTION", "Motion detected in " + name);
                }

                // Auto turn off after 30 sec
                long idle = System.currentTimeMillis() - lastMotion;
                if (idle > 30000) {
                     if (linkedRoom != null) {
                        for (Device device : linkedRoom.getDevices()) {
                            if (device instanceof Light light && light.isLinked()) {
                                light.turnOff();
                            }
                        }
                    }
                }
                
                try { Thread.sleep(30); } catch (InterruptedException e) { break; }
            }
        }).start();
    }

    public String saveSnapshot(Mat frame, String type) {
        String filename = "snapshot_" + System.currentTimeMillis() + ".jpg";
        String path = "src/main/resources/snapshots/" + filename;
        File dir = new File("src/main/resources/snapshots/");
        if (!dir.exists()) dir.mkdirs();
        
        Imgcodecs.imwrite(path, frame);
        DatabaseManager.logEvent(this.id, type, "Snapshot captured for " + type);
        System.out.println("Snapshot saved: " + path);
        return filename;
    }


    // FACE DETECTION, RECOGNITION, CHILD SAFETY THREAD

    private boolean detectDangerous(Mat frame) {
        if (frame.empty()) return false;

        boolean dangerDetected = false;

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        // Blur to reduce noise
        Imgproc.GaussianBlur(gray, gray, new Size(5,5), 0);

        // Detect edges using Canny
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 150);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            // Approximate the contour to a polygon
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double peri = Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true);

            // Heuristic: sharp objects often have long thin shape
            Rect bounding = Imgproc.boundingRect(new MatOfPoint(approx.toArray()));
            double aspectRatio = (double) bounding.width / bounding.height;
            double area = Imgproc.contourArea(contour);

            // Re-tuned for knives: more inclusive aspect ratio, lower min area
            if (area > 200 && area < 6000 && (aspectRatio < 0.35 || aspectRatio > 2.8)) {
                // draw rectangle
                Imgproc.rectangle(frame, bounding.tl(), bounding.br(), new Scalar(0,0,255), 2);
                System.out.println("⚠ Danger detected (possible sharp object) - Area: " + (int)area + ", AR: " + String.format("%.2f", aspectRatio));
                dangerDetected = true;
            }
        }

        return dangerDetected;
    }

    private void startFaceRecognition() {
        new Thread(() -> {

            if (!VC.isOpened()) {
                System.out.println("Camera not opened for face recognition");
                return;
            }


            Mat frame = new Mat();
            Size inputSize = new Size(320, 320); // YuNet input size

            FaceDetectorYN detector = FaceDetectorYN.create(faceModel, "", inputSize);
            detector.setInputSize(inputSize);

            FaceRecognizerSF recognizer = FaceRecognizerSF.create(recogModel, "");

            System.out.println("Face recognition started...");

            while (isRunning) {
                try {
                    synchronized (VC) {
                        if (VC == null || !VC.isOpened()) break;
                        if (!VC.read(frame) || frame.empty()) {
                            Thread.sleep(100);
                            continue;
                        }
                    }
                } catch (Exception e) { break; }

                Mat frameClone = frame.clone(); // for dangerous object detection so that it gets the full image

                // --- Resize while preserving an aspect ratio ---
                Mat resized = new Mat();
                double scale = Math.min(inputSize.width / frame.cols(), inputSize.height / frame.rows());
                int newWidth = (int)(frame.cols() * scale);
                int newHeight = (int)(frame.rows() * scale);
                Imgproc.resize(frame, resized, new Size(newWidth, newHeight));

                // Pad to square if needed
                int top = (int) ((inputSize.height - newHeight) / 2);
                int bottom = (int) (inputSize.height - newHeight - top);
                int left = (int) ((inputSize.width - newWidth) / 2);
                int right = (int) (inputSize.width - newWidth - left);
                Core.copyMakeBorder(resized, resized, top, bottom, left, right, Core.BORDER_CONSTANT, new Scalar(0,0,0));

                // --- Convert to grayscale + improves lighting ---
                Mat gray = new Mat();
                Imgproc.cvtColor(resized, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.equalizeHist(gray, gray);

                Mat detections = new Mat();
                detector.detect(resized, detections);

                if (detections.rows() == 0) continue;

                boolean childDetected = false;
                boolean unknownDetected = false;

                for (int i = 0; i < detections.rows(); i++) {

                    Mat aligned = new Mat();
                    Mat embedding = new Mat();

                    recognizer.alignCrop(resized, detections.row(i), aligned);
                    recognizer.feature(aligned, embedding);

                    double bestScore = Double.MAX_VALUE;
                    String bestMatch = null;
                    String bestGroup = null;

                    // --- check children ---
                    for (String child : childknownEmbeddings.keySet()) {
                        double score = recognizer.match(
                                childknownEmbeddings.get(child),
                                embedding,
                                FaceRecognizerSF.FR_NORM_L2
                        );
                        if (score < bestScore) {
                            bestScore = score;
                            bestMatch = child;
                            bestGroup = "CHILD";
                        }
                    }

                    // --- check family ---
                    for (String member : familyknownEmbeddings.keySet()) {
                        double score = recognizer.match(
                                familyknownEmbeddings.get(member),
                                embedding,
                                FaceRecognizerSF.FR_NORM_L2
                        );
                        if (score < bestScore) {
                            bestScore = score;
                            bestMatch = member;
                            bestGroup = "FAMILY";
                        }
                    }

                    // --- decision ---
                    if (bestScore < 1.05) {
                        if ("CHILD".equals(bestGroup)) {
                            childDetected = true;
                            if (linkedRoom != null) {
                                DatabaseManager.updateLocation(bestMatch, linkedRoom.getId());
                            }
                        } else {
                        }
                        // familyDetected = true;
                        // detectedPerson = bestMatch;
                    } else {
                        unknownDetected = true;
                    }
                }

                // --- Dangerous object detection ---
                boolean dangerDetected = detectDangerous(frameClone);

                // --- Alerts ---

                if (unknownDetected) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - lastAlertTime.getOrDefault("SECURITY", 0L) > 120000) {
                        giveImgWarningToModule("Security", frameClone);
                        String snap = saveSnapshot(frameClone, "UNKNOWN_PERSON");
                        if (linkedRoom != null) {
                            DatabaseManager.saveAlert(linkedRoom.getId(), "SECURITY", snap, "Unknown person detected in " + name);
                        }
                        lastAlertTime.put("SECURITY", nowTime);
                    }
                }

                if (dangerDetected) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - lastAlertTime.getOrDefault("DANGER", 0L) > 120000) {
                        String msg = childDetected ? "Child near potential danger in " + name : "Dangerous object detected in " + name;
                        giveImgWarningToModule("Child", frameClone);
                        String snap = saveSnapshot(frameClone, "DANGER");
                        if (linkedRoom != null) {
                            DatabaseManager.saveAlert(linkedRoom.getId(), "DANGER", snap, msg);
                        }
                        lastAlertTime.put("DANGER", nowTime);
                    }
                }
                
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }

        }).start();
    }
}