package smartHome.app;

import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;

import smartHome.db.DatabaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Camera extends Device {

    private volatile boolean isRunning = false;
    private VideoCapture VC;

    // COCO Dataset class names (80 classes, 0-indexed)
    private static final String[] COCO_CLASSES = {
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse",
        "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie",
        "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove",
        "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon",
        "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut",
        "cake", "chair", "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
        "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book",
        "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    };

    //models
    private final String faceModel = "src/main/resources/models/face_detection_yunet_2023mar.onnx";
    private final String recogModel = "src/main/resources/models/face_recognition_sface_2021dec.onnx";
    private final String yoloModel = "src/main/resources/models/yolov8n.onnx";

    private Net yoloNet;

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
                
                // Initialize YOLO
                try {
                    yoloNet = Dnn.readNetFromONNX(yoloModel);
                    System.out.println("✔ YOLO Object Detection loaded.");
                } catch (Exception e) {
                    System.out.println("⚠ Failed to load YOLO: " + e.getMessage());
                }

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

            // Simple blunt resize to match live recognition
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

    public void loadKnownFaces() {
        familyknownEmbeddings.clear();
        childknownEmbeddings.clear();

        File family_dir = new File("src/main/resources/faces/family");
        File[] family_files = family_dir.listFiles();
        loadFacesFromFiles(family_files, familyknownEmbeddings);

        File child_dir = new File("src/main/resources/faces/child");
        File[] child_files = child_dir.listFiles();
        loadFacesFromFiles(child_files, childknownEmbeddings);
        
        System.out.println("ℹ System Memory: " + familyknownEmbeddings.size() + " Family, " + childknownEmbeddings.size() + " Children loaded.");
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

    private String getClassName(int classId) {
        if (classId >= 0 && classId < COCO_CLASSES.length) {
            return COCO_CLASSES[classId];
        }
        return "Unknown (" + classId + ")";
    }

    private List<String> detectDangerous(Mat frame) {
        List<String> detectedObjects = new ArrayList<>();
        
        if (frame.empty()) {
            System.out.println("⚠ YOLO: Frame is empty");
            return detectedObjects;
        }
        if (yoloNet == null) {
            System.out.println("⚠ YOLO: Network not loaded");
            return detectedObjects;
        }

        // --- YOLO Pre-processing ---
        Mat blob = Dnn.blobFromImage(frame, 1/255.0, new Size(640, 640), new Scalar(0,0,0), true, false);
        yoloNet.setInput(blob);
        
        List<Mat> outputs = new ArrayList<>();
        yoloNet.forward(outputs, yoloNet.getUnconnectedOutLayersNames());
        
        if (outputs.isEmpty()) {
            System.out.println("⚠ YOLO: No outputs from network");
            return detectedObjects;
        }
        
        Mat output = outputs.get(0); // [1, 84, 8400]
        System.out.println("ℹ YOLO Output shape: " + output.size());
        
        // Reshape and Transpose: [1, 84, 8400] -> [84, 8400] -> [8400, 84]
        Mat reshaped = output.reshape(1, 84);
        Mat transposed = new Mat();
        Core.transpose(reshaped, transposed);

        List<Rect2d> boxes = new ArrayList<>();
        List<Float> confidences = new ArrayList<>();
        List<Integer> classIds = new ArrayList<>();

        float confThreshold = 0.25f; // General detection threshold
        float dangerThreshold = 0.15f; // Lower threshold for dangerous objects

        int detectionCount = 0;
        for (int i = 0; i < transposed.rows(); i++) {
            Mat row = transposed.row(i);
            Mat scores = row.colRange(4, 84);
            Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
            float maxScore = (float) mm.maxVal;
            int classId = (int) mm.maxLoc.x;

            // Use lower threshold for dangerous objects
            boolean isDangerous = (classId == 43 || classId == 76 || classId == 69 || classId == 42);
            float threshold = isDangerous ? dangerThreshold : confThreshold;

            if (maxScore > threshold) {
                detectionCount++;
                float cx = (float) row.get(0, 0)[0];
                float cy = (float) row.get(0, 1)[0];
                float w = (float) row.get(0, 2)[0];
                float h = (float) row.get(0, 3)[0];

                // Scale to frame
                double x = (cx - w/2) * frame.cols() / 640.0;
                double y = (cy - h/2) * frame.rows() / 640.0;
                double width = w * frame.cols() / 640.0;
                double height = h * frame.rows() / 640.0;

                boxes.add(new Rect2d(x, y, width, height));
                confidences.add(maxScore);
                classIds.add(classId);
                
                // Log dangerous object detections prominently
                if (isDangerous) {
                    System.out.println(String.format("  ⚠ DANGEROUS: Class %d (%s) - Conf: %.2f", 
                        classId, getClassName(classId), maxScore));
                } else {
                    System.out.println(String.format("  Detection: Class %d (%s) - Conf: %.2f", 
                        classId, getClassName(classId), maxScore));
                }
            }
        }
        
        System.out.println("ℹ YOLO: Found " + detectionCount + " detections (danger threshold: " + dangerThreshold + ", general: " + confThreshold + ")");

        if (!boxes.isEmpty()) {
            MatOfRect2d boxesMat = new MatOfRect2d();
            boxesMat.fromList(boxes);
            
            float[] confArr = new float[confidences.size()];
            for(int i=0; i<confidences.size(); i++) confArr[i] = confidences.get(i);
            MatOfFloat confMat = new MatOfFloat();
            confMat.fromArray(confArr);

            MatOfInt indices = new MatOfInt();
            // Use lower NMS threshold to keep more dangerous object detections
            Dnn.NMSBoxes(boxesMat, confMat, dangerThreshold, 0.50f, indices);

            for (int idx : indices.toArray()) {
                int cId = classIds.get(idx);
                // Dangerous objects in COCO dataset:
                // - Knife: 43, Scissors: 76, Oven: 69, Fork: 42
                if (cId == 43 || cId == 76 || cId == 69 || cId == 42) {
                    String objectName = getClassName(cId);
                    detectedObjects.add(objectName);
                    
                    Rect2d box = boxes.get(idx);
                    // Draw thick red rectangle around dangerous object
                    Imgproc.rectangle(frame, new Point(box.x, box.y), new Point(box.x + box.width, box.y + box.height), new Scalar(0,0,255), 4);
                    // Add bold text label above the box
                    String label = "⚠ " + objectName.toUpperCase() + " ⚠";
                    Imgproc.putText(frame, label, new Point(box.x, box.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0,0,255), 3);
                    System.out.println("🚨 DANGER DETECTED: " + objectName.toUpperCase() + " - Confidence: " + String.format("%.2f", confidences.get(idx)));
                }
            }
        }

        return detectedObjects;
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

                Mat resized = new Mat();
                Imgproc.resize(frame, resized, inputSize);

                // --- Convert to grayscale (Original code used this for light correction check) ---
                // We will use the original resized BGR frame for YuNet and SFace as they are optimized for it.

                Mat detections = new Mat();
                detector.detect(resized, detections);

                boolean childDetected = false;
                boolean unknownDetected = false;

                if (detections.rows() > 0) {
                    for (int i = 0; i < detections.rows(); i++) {
                        Mat aligned = new Mat();
                        Mat embedding = new Mat();

                        recognizer.alignCrop(resized, detections.row(i), aligned);
                        recognizer.feature(aligned, embedding);

                        double bestChildScore = Double.MAX_VALUE;
                        String bestChildMatch = null;
                        double bestFamilyScore = Double.MAX_VALUE;
                        String bestFamilyMatch = null;

                        // --- check children ---
                        for (String child : childknownEmbeddings.keySet()) {
                            double score = recognizer.match(childknownEmbeddings.get(child), embedding, FaceRecognizerSF.FR_NORM_L2);
                            if (score < bestChildScore) {
                                bestChildScore = score;
                                bestChildMatch = child;
                            }
                        }

                        // --- check family ---
                        for (String member : familyknownEmbeddings.keySet()) {
                            double score = recognizer.match(familyknownEmbeddings.get(member), embedding, FaceRecognizerSF.FR_NORM_L2);
                            if (score < bestFamilyScore) {
                                bestFamilyScore = score;
                                bestFamilyMatch = member;
                            }
                        }

                        // Determine overall best
                        double bestScore = Math.min(bestChildScore, bestFamilyScore);
                        String bestMatch = (bestChildScore < bestFamilyScore) ? bestChildMatch : bestFamilyMatch;
                        String bestGroup = (bestChildScore < bestFamilyScore) ? "CHILD" : "FAMILY";

                        // Safety Priority: If a child is even remotely visible (< 1.35), we should treat them as present for danger alerts
                        if (bestChildScore < 1.35) {
                            childDetected = true;
                        }

                        // --- decision ---
                        System.out.println(String.format("DEBUG Match - Best Child: %s (%.3f) | Best Family: %s (%.3f)", 
                            bestChildMatch, bestChildScore, bestFamilyMatch, bestFamilyScore));
                        System.out.println(String.format("  → Overall Best Match: %s [%s] (%.3f)", 
                            bestMatch, bestGroup, bestScore));

                        if (bestScore < 1.20) {
                            if (linkedRoom != null) {
                                DatabaseManager.updateLocation(bestMatch, linkedRoom.getId());
                            }
                            if ("CHILD".equals(bestGroup)) {
                                childDetected = true;
                            }
                        } else {
                            unknownDetected = true;
                        }
                    }
                }

                // --- Dangerous object detection ---
                List<String> dangerousObjects = detectDangerous(frameClone);
                boolean dangerDetected = !dangerousObjects.isEmpty();

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

                if (dangerDetected && (childDetected || unknownDetected)) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - lastAlertTime.getOrDefault("DANGER", 0L) > 120000) {
                        String personType = childDetected ? "Child" : "Unknown Person";
                        // Create specific message with detected objects
                        String objectsList = String.join(", ", dangerousObjects);
                        String msg = personType + " near " + objectsList + " in " + name;
                        giveImgWarningToModule("Child", frameClone);
                        String snap = saveSnapshot(frameClone, "DANGER");
                        if (linkedRoom != null) {
                            DatabaseManager.saveAlert(linkedRoom.getId(), "DANGER", snap, msg);
                        }
                        lastAlertTime.put("DANGER", nowTime);
                        System.out.println("🚨 DANGER Alert fired: " + msg);
                    }
                }
                
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }

        }).start();
    }
}