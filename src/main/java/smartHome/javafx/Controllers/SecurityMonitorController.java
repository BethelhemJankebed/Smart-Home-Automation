package smartHome.javafx.Controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import smartHome.app.Camera;
import smartHome.app.Device;
import smartHome.app.Room;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

public class SecurityMonitorController {

    private VBox root;
    private ComboBox<Room> roomSelector;
    private ImageView videoView;
    private Camera activeCamera;
    private AnimationTimer timer;
    private VBox alertContainer;

    public SecurityMonitorController() {
        root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #64748b; -fx-font-size: 16px; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            cleanup();
            SceneManager.switchScene("Dashboard");
        });

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("Security Command Center");
        mainTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label subTitle = new Label("Global surveillance and unknown person detection");
        subTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(mainTitle, subTitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button registerBtn = new Button("Register Face");
        registerBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        registerBtn.setOnAction(e -> handleRegisterFace());

        header.getChildren().addAll(backBtn, titleBox, spacer, registerBtn);

        // Content
        HBox content = new HBox(30);
        content.setAlignment(Pos.CENTER);
        
        // 1. Live Feed Card
        VBox feedCard = new VBox(15);
        feedCard.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");
        
        HBox feedHeader = new HBox(10);
        feedHeader.setAlignment(Pos.CENTER_LEFT);
        Label liveIndicator = new Label("●");
        liveIndicator.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14px;");
        Label feedLabel = new Label("SECURITY FEED");
        feedLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #1e293b; -fx-font-size: 11px;");
        
        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);

        roomSelector = new ComboBox<>();
        roomSelector.setPromptText("Select Camera...");
        roomSelector.setPrefWidth(180);
        roomSelector.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-font-size: 11px;");
        
        List<Room> rooms = DatabaseManager.getAllRooms();
        roomSelector.getItems().addAll(rooms);
        roomSelector.setOnAction(e -> {
            Room selected = roomSelector.getValue();
            if (selected != null) {
                if (activeCamera != null) activeCamera.turnOff();
                switchCameraToRoom(selected);
            }
        });

        if (!rooms.isEmpty()) {
            roomSelector.setValue(rooms.get(0));
            switchCameraToRoom(rooms.get(0));
        }

        feedHeader.getChildren().addAll(liveIndicator, feedLabel, s1, roomSelector);
        
        videoView = new ImageView();
        videoView.setFitWidth(550);
        videoView.setFitHeight(350);
        videoView.setPreserveRatio(true);
        
        StackPane videoFrame = new StackPane();
        videoFrame.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 15;");
        
        Label noSignal = new Label("OFFLINE");
        noSignal.setStyle("-fx-text-fill: #334155; -fx-font-weight: 800;");
        Label recTag = new Label("• SECURITY");
        recTag.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: 900; -fx-font-size: 12px;");
        StackPane.setAlignment(recTag, Pos.TOP_RIGHT);
        StackPane.setMargin(recTag, new Insets(15));
        videoFrame.getChildren().addAll(noSignal, videoView, recTag);

        Rectangle clip = new Rectangle(550, 350);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        videoFrame.setClip(clip);
        
        feedCard.getChildren().addAll(feedHeader, videoFrame);

        // 2. Alerts Sidebar
        VBox alertsCard = new VBox(15);
        alertsCard.setPrefWidth(350);
        alertsCard.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");
        
        Label alertsHeader = new Label("UNKNOWN PERSON LOGS");
        alertsHeader.setStyle("-fx-font-weight: 900; -fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        
        ScrollPane scroll = new ScrollPane();
        alertContainer = new VBox(15);
        scroll.setContent(alertContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        alertsCard.getChildren().addAll(alertsHeader, scroll);

        content.getChildren().addAll(feedCard, alertsCard);
        root.getChildren().addAll(header, content);

        startPolling();
    }

    private void handleRegisterFace() {
        if (activeCamera == null) {
            showAlert("No Camera Active", "Please select a room with an active camera first.");
            return;
        }
        
        Mat frame = activeCamera.getLatestFrame();
        if (frame == null || frame.empty()) {
            showAlert("No Frame Captured", "The camera hasn't captured a frame yet.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Register New Face");
        dialog.setHeaderText("New Person Detected");
        dialog.setContentText("Enter name for this person:");
        
        dialog.showAndWait().ifPresent(name -> {
            String path = "src/main/resources/Faces/family/" + name + ".jpg";
            File dir = new File("src/main/resources/Faces/family/");
            if (!dir.exists()) dir.mkdirs();
            
            Imgcodecs.imwrite(path, frame);
            showAlert("Success", name + " has been registered successfully!");
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void switchCameraToRoom(Room room) {
        List<Device> devices = DatabaseManager.getDevicesForRoom(room.getId());
        Camera found = null;
        for (Device d : devices) {
            if (d instanceof Camera) {
                found = (Camera) d;
                break;
            }
        }
        
        if (found != null) {
            activeCamera = found;
            activeCamera.setLinkedRoom(room);
            activeCamera.turnOn();
        }
    }

    private void startPolling() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (activeCamera != null) {
                    Mat frame = activeCamera.getLatestFrame();
                    if (frame != null && !frame.empty()) {
                        videoView.setImage(mat2Image(frame));
                    }
                }
                
                if (now - lastUpdate > 1_500_000_000L) {
                    updateAlertsUI();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    private void updateAlertsUI() {
        Room selected = roomSelector.getValue();
        if (selected == null || alertContainer == null) return;

        List<String[]> alerts = DatabaseManager.getRecentAlertsForRoomByType(selected.getId(), "SECURITY");
        
        Platform.runLater(() -> {
            if (alerts.isEmpty()) {
                alertContainer.getChildren().clear();
                Label none = new Label("No unknown person detected");
                none.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
                alertContainer.getChildren().add(none);
                return;
            }

            if (alertContainer.getChildren().size() == alerts.size()) return;

            alertContainer.getChildren().clear();
            for (String[] alert : alerts) {
                alertContainer.getChildren().add(createAlertCard(alert[0], alert[1], alert[2]));
            }
        });
    }

    private VBox createAlertCard(String time, String path, String msg) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 15; -fx-border-color: #cbd5e1; -fx-border-radius: 15;");
        
        Label tLbl = new Label("🕒 " + time);
        tLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 900; -fx-text-fill: #3b82f6;");
        
        Label mLbl = new Label(msg);
        mLbl.setWrapText(true);
        mLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        VBox snapFrame = new VBox();
        snapFrame.setAlignment(Pos.CENTER);
        try {
            String fullPath = new File("src/main/resources/snapshots/" + path).toURI().toString();
            Image img = new Image(fullPath, 250, 0, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(240);
            iv.setPreserveRatio(true);
            
            Rectangle snclip = new Rectangle(240, 140);
            snclip.setArcWidth(20);
            snclip.setArcHeight(20);
            iv.setClip(snclip);
            snapFrame.getChildren().add(iv);
        } catch (Exception e) {}
        
        card.getChildren().addAll(tLbl, snapFrame, mLbl);
        return card;
    }

    private void cleanup() {
        if (timer != null) timer.stop();
        if (activeCamera != null) activeCamera.turnOff();
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public VBox getView() {
        return root;
    }
}

