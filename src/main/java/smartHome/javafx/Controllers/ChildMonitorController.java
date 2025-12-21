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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

public class ChildMonitorController {

    private VBox root;
    private ImageView videoView;
    private Camera activeCamera;
    private AnimationTimer timer;
    private ComboBox<Room> roomSelector;

    private Label currentRoom;
    private Label currentTime;
    private VBox alertContainer;

    public ChildMonitorController() {
        root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #fdf2f8, #f8fafc);");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 18px; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            cleanup();
            SceneManager.switchScene("Dashboard");
        });
        
        VBox titleBox = new VBox(2);
        Label title = new Label("Child Monitor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Real-time safety and activity tracking");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(backBtn, titleBox);

        // Content
        HBox content = new HBox(30);
        content.setAlignment(Pos.CENTER);
        
        // 1. Live Feed Card
        VBox feedCard = new VBox(15);
        feedCard.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");
        
        HBox feedHeader = new HBox(10);
        feedHeader.setAlignment(Pos.CENTER_LEFT);
        Label liveIndicator = new Label("●");
        liveIndicator.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
        Label feedLabel = new Label("LIVE CAMERA FEED");
        feedLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #1e293b; -fx-font-size: 11px;");
        
        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);

        roomSelector = new ComboBox<>();
        roomSelector.setPromptText("Select Source...");
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
        videoView.setFitWidth(520);
        videoView.setFitHeight(320);
        videoView.setPreserveRatio(true);
        
        StackPane videoFrame = new StackPane();
        videoFrame.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 15;");
        
        Label noSignal = new Label("NO SIGNAL");
        noSignal.setStyle("-fx-text-fill: #334155; -fx-font-weight: 800;");
        
        Label recTag = new Label("• REC");
        recTag.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 900; -fx-font-size: 12px;");
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
        statusCard.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");
        
        Label statusHeader = new Label("LATEST DETECTION");
        statusHeader.setStyle("-fx-font-weight: 900; -fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        VBox locBox = createPremiumInfoTile("Current Room", "📍", currentRoom = new Label("Tracking..."), "#eff6ff", "#3b82f6");
        VBox timeBox = createPremiumInfoTile("Last Seen", "🕒", currentTime = new Label("--:--"), "#f0fdf4", "#10b981");

        statusCard.getChildren().addAll(statusHeader, locBox, timeBox);
        infoSide.getChildren().add(statusCard);

        // 3. Alerts Sidebar
        VBox alertsCard = new VBox(15);
        alertsCard.setPrefWidth(320);
        alertsCard.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");
        
        Label alertsHeader = new Label("SAFETY NOTIFICATIONS");
        alertsHeader.setStyle("-fx-font-weight: 900; -fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane();
        alertContainer = new VBox(15);
        scroll.setContent(alertContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        alertsCard.getChildren().addAll(alertsHeader, scroll);

        content.getChildren().addAll(feedCard, infoSide, alertsCard);
        root.getChildren().add(content);

        startPolling();
    }

    private VBox createPremiumInfoTile(String label, String icon, Label value, String bg, String accent) {
        VBox tile = new VBox(8);
        tile.setPadding(new Insets(15));
        tile.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12;");
        
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");
        Label titleLbl = new Label(label.toUpperCase());
        titleLbl.setStyle("-fx-font-weight: 900; -fx-text-fill: #64748b; -fx-font-size: 10px;");
        row.getChildren().addAll(iconLbl, titleLbl);
        
        value.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        tile.getChildren().addAll(row, value);
        return tile;
    }

    private void switchCameraToRoom(Room room) {
        // Find a camera in the selected room
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
            System.out.println("Switched to camera: " + activeCamera.getName());
        } else {
            // Show mock/notice?
            System.out.println("No camera found in " + room.getName());
        }
    }

    private void startPolling() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                // Update Camera Feed
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

        List<String[]> alerts = DatabaseManager.getRecentAlertsForRoomByType(selected.getId(), "DANGER");
        
        javafx.application.Platform.runLater(() -> {
            if (alerts.isEmpty()) {
                alertContainer.getChildren().clear();
                Label none = new Label("No recent security alerts");
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
        card.setStyle("-fx-background-color: #fff1f2; -fx-background-radius: 15; -fx-border-color: #fecdd3; -fx-border-radius: 15;");
        
        Label tLbl = new Label("🕒 " + time);
        tLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: 900; -fx-text-fill: #e11d48;");
        
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
