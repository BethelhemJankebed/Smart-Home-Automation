package smartHome.javafx.Controllers;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import smartHome.app.Camera;
import smartHome.db.DatabaseManager;
import smartHome.app.Device;
import smartHome.app.Room;
import smartHome.javafx.Scene.SceneManager;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream; //→ used to convert bytes from DB into Image

import java.util.List;

public class ChildMonitorController {

    private VBox root;
    private ImageView videoView;
    private Camera activeCamera;
    private AnimationTimer timer;
    private ComboBox<Room> roomSelector;
    private ComboBox<Camera> cameraSelector;

    private Label currentRoom;
    private Label currentTime;
    private VBox alertContainer;

    public ChildMonitorController() {
        root = new VBox(25);
        root.getStyleClass().add("root-monitor");

        // Header
        HBox header = new HBox(20);
        header.getStyleClass().add("header-container");

        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            cleanup();
            SceneManager.switchScene("Dashboard");
        });
        
        VBox titleBox = new VBox(2);
        Label title = new Label("Child Monitor");
        title.getStyleClass().add("monitor-title");
        Label subtitle = new Label("Real-time safety and activity tracking");
        subtitle.getStyleClass().add("monitor-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(backBtn, titleBox);

        // Content
        HBox content = new HBox(30);
        content.setAlignment(Pos.CENTER);
        
        // 1. Live Feed Card
        VBox feedCard = new VBox(15);
        feedCard.getStyleClass().add("feed-card");
        
        HBox feedHeader = new HBox(10);
        feedHeader.setAlignment(Pos.CENTER_LEFT);
        Label liveIndicator = new Label("●");
        liveIndicator.getStyleClass().add("live-indicator");
        
        Label feedLabel = new Label("LIVE CAMERA FEED");
        feedLabel.getStyleClass().add("feed-label");
        
        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);

        roomSelector = new ComboBox<>();
        roomSelector.setPromptText("Room...");
        roomSelector.setPrefWidth(120);
        roomSelector.getStyleClass().add("selector-combo");
        
        roomSelector.setConverter(new javafx.util.StringConverter<Room>() {
            @Override public String toString(Room r) { return r == null ? "" : r.getName(); }
            @Override public Room fromString(String s) { return null; }
        });
        
        List<Room> rooms = DatabaseManager.getAllRooms();
        roomSelector.getItems().addAll(rooms);
        roomSelector.setOnAction(e -> {
            Room selected = roomSelector.getValue();
            if (selected != null) {
                if (activeCamera != null) activeCamera.turnOff();
                switchCameraToRoom(selected);
            }
        });

        cameraSelector = new ComboBox<>();
        cameraSelector.setPromptText("Camera...");
        cameraSelector.setPrefWidth(120);
        cameraSelector.getStyleClass().add("selector-combo");
        cameraSelector.setConverter(new javafx.util.StringConverter<Camera>() {
            @Override public String toString(Camera c) { return c == null ? "" : c.getName(); }
            @Override public Camera fromString(String s) { return null; }
        });
        cameraSelector.setOnAction(e -> {
            Camera selected = cameraSelector.getValue();
            if (selected != null && selected != activeCamera) {
                if (activeCamera != null) activeCamera.turnOff();
                activeCamera = selected;
                activeCamera.setLinkedRoom(roomSelector.getValue());
                activeCamera.turnOn();
            }
        });

        if (!rooms.isEmpty()) {
            roomSelector.setValue(rooms.get(0));
            switchCameraToRoom(rooms.get(0));
        }

        feedHeader.getChildren().addAll(liveIndicator, feedLabel, s1, cameraSelector, roomSelector);
        
        videoView = new ImageView();
        videoView.setFitWidth(520);
        videoView.setFitHeight(320);
        videoView.setPreserveRatio(true);
        
        StackPane videoFrame = new StackPane();
        videoFrame.getStyleClass().add("video-frame");
        
        Label noSignal = new Label("NO SIGNAL");
        noSignal.getStyleClass().add("no-signal-label");
        
        Label recTag = new Label("• REC");
        recTag.getStyleClass().add("rec-tag");
        StackPane.setAlignment(recTag, Pos.TOP_RIGHT);
        StackPane.setMargin(recTag, new Insets(15));

        videoFrame.getChildren().addAll(noSignal, videoView, recTag);

        // Clip to rounded corners
        Rectangle clip = new Rectangle(520, 320);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        videoFrame.setClip(clip);
        
        feedCard.getChildren().addAll(feedHeader, videoFrame);

        // 2. Detection Info Card
        VBox infoSide = new VBox(20);
        infoSide.setPrefWidth(300);

        VBox statusCard = new VBox(15);
        statusCard.getStyleClass().add("status-card");
        
        Label statusHeader = new Label("LATEST DETECTION");
        statusHeader.getStyleClass().add("status-header");

        VBox locBox = createPremiumInfoTile("Current Room", "📍", currentRoom = new Label("Tracking..."), "info-tile-blue");
        VBox timeBox = createPremiumInfoTile("Last Seen", "🕒", currentTime = new Label("--:--"), "info-tile-green");

        statusCard.getChildren().addAll(statusHeader, locBox, timeBox);
        infoSide.getChildren().add(statusCard);

        // 3. Alerts Sidebar
        VBox alertsCard = new VBox(15);
        alertsCard.setPrefWidth(320);
        alertsCard.getStyleClass().add("alerts-card");
        
        Label alertsHeader = new Label("SAFETY NOTIFICATIONS");
        alertsHeader.getStyleClass().add("alerts-header");
        
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane();
        alertContainer = new VBox(15);
        scroll.setContent(alertContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        alertsCard.getChildren().addAll(alertsHeader, scroll);

        content.getChildren().addAll(feedCard, infoSide, alertsCard);
        root.getChildren().addAll(header, content);
        
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/monitor.css").toExternalForm());

        startPolling();
    }

    private VBox createPremiumInfoTile(String label, String icon, Label value, String colorClass) {
        VBox tile = new VBox(8);
        tile.getStyleClass().addAll("info-tile-base", colorClass);
        
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("tile-icon");
        
        Label titleLbl = new Label(label.toUpperCase());
        titleLbl.getStyleClass().add("tile-label");
        row.getChildren().addAll(iconLbl, titleLbl);
        
        value.getStyleClass().add("tile-value-base");
        
        tile.getChildren().addAll(row, value);
        return tile;
    }

    private void switchCameraToRoom(Room room) {
        List<Device> devices = DatabaseManager.getDevicesForRoom(room.getId());
        List<Camera> cameras = devices.stream()
                .filter(d -> d instanceof Camera)
                .map(d -> (Camera) d)
                .toList();

        cameraSelector.getItems().clear();
        cameraSelector.getItems().addAll(cameras);

        if (!cameras.isEmpty()) {
           
            activeCamera = cameras.get(0);
            activeCamera.setLinkedRoom(room);
            activeCamera.turnOn();
            cameraSelector.setValue(cameras.get(0));
            System.out.println("Switched to camera: " + activeCamera.getName());
        } else {
            activeCamera = null;
            cameraSelector.setPromptText("No Camera");
            System.out.println("No camera found in " + room.getName());
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
                        Image img = mat2Image(frame);
                        videoView.setImage(img);
                    }
                }
                
                // Update Location & Alerts every ~1 second
                if (now - lastUpdate > 1_500_000_000L) {
                    updateLocationUI();
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

        // Only get DANGER alerts (most recent first - already sorted by database)
        List<String[]> alerts = DatabaseManager.getRecentAlertsForRoomByType(selected.getId(), "DANGER");
        
        javafx.application.Platform.runLater(() -> {
            if (alerts.isEmpty()) {
                alertContainer.getChildren().clear();
                Label none = new Label("No recent danger alerts");
                none.getStyleClass().add("no-alerts-label");
                alertContainer.getChildren().add(none);
                return;
            }

            alertContainer.getChildren().clear();
            for (String[] alert : alerts) {
                alertContainer.getChildren().add(createAlertCard(alert[0], alert[1], alert[2]));
            }
        });
    }

    private VBox createAlertCard(String time, String alertIdStr, String msg) {
        VBox card = new VBox(10);
        card.getStyleClass().add("alert-card");
        
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label tLbl = new Label("🕒 " + time);
        tLbl.getStyleClass().add("alert-time");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("alert-delete-btn");
        deleteBtn.setOnAction(e -> {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, "Delete this notification?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.YES) {
                    try {
                        int alertId = Integer.parseInt(alertIdStr);
                        DatabaseManager.deleteAlertById(alertId);
                        updateAlertsUI();
                    } catch (Exception ex) {
                        System.err.println("UI_DEBUG: Failed to delete alert: " + ex.getMessage());
                    }
                }
            });
        });
        
        headerRow.getChildren().addAll(tLbl, spacer, deleteBtn);
        
        Label mLbl = new Label(msg);
        mLbl.setWrapText(true);
        mLbl.getStyleClass().add("alert-msg");
        
        VBox snapFrame = new VBox();
        snapFrame.setAlignment(Pos.CENTER);
        try {
            int alertId = Integer.parseInt(alertIdStr);
            byte[] imgData = DatabaseManager.getAlertSnapshot(alertId);
            
            if (imgData != null && imgData.length > 0) {
                Image img = new Image(new ByteArrayInputStream(imgData), 250, 0, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(240);
                iv.setPreserveRatio(true);
                
                Rectangle snclip = new Rectangle(240, 140);
                snclip.setArcWidth(20);
                snclip.setArcHeight(20);
                iv.setClip(snclip);
                snapFrame.getChildren().add(iv);
            }
        } catch (Exception e) { 
            e.printStackTrace();
        }
        
        card.getChildren().addAll(headerRow, snapFrame, mLbl);
        return card;
    }
    
    private void updateLocationUI() {
        String[] locationData = DatabaseManager.getLastLocation("child"); 
        if (locationData != null) {
            javafx.application.Platform.runLater(() -> {
                currentRoom.setText(locationData[0]);
                currentTime.setText(locationData[1]);
            });
        }
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
