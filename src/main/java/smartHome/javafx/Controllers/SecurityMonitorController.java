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
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javafx.stage.FileChooser;

public class SecurityMonitorController {

    private VBox root;
    private ComboBox<Room> roomSelector;
    private ComboBox<Camera> cameraSelector;
    private ImageView videoView;
    private Camera activeCamera;
    private AnimationTimer timer;
    private VBox alertContainer;

    public SecurityMonitorController() {
        root = new VBox(25);
        root.getStyleClass().add("root-security");

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
        Label mainTitle = new Label("Security Command Center");
        mainTitle.getStyleClass().add("security-title");
        Label subTitle = new Label("Global surveillance and unknown person detection");
        subTitle.getStyleClass().add("security-subtitle");
        titleBox.getChildren().addAll(mainTitle, subTitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button manageBtn = new Button("Manage Faces");
        manageBtn.getStyleClass().add("manage-btn");
        manageBtn.setOnAction(e -> handleManageFaces());
        
        Button registerBtn = new Button("Register Face");
        registerBtn.getStyleClass().add("register-btn");
        registerBtn.setOnAction(e -> handleRegisterFace());

        header.getChildren().addAll(backBtn, titleBox, spacer, manageBtn, registerBtn);

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
        Label feedLabel = new Label("SECURITY FEED");
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

        feedHeader.getChildren().addAll(liveIndicator, feedLabel, s1, cameraSelector, roomSelector);
        
        videoView = new ImageView();
        videoView.setFitWidth(550);
        videoView.setFitHeight(350);
        videoView.setPreserveRatio(true);
        
        StackPane videoFrame = new StackPane();
        videoFrame.getStyleClass().add("video-frame");
        
        Label noSignal = new Label("OFFLINE");
        noSignal.getStyleClass().add("no-signal-label");
        Label recTag = new Label("• SECURITY");
        recTag.getStyleClass().add("rec-tag");
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
        alertsCard.getStyleClass().add("alerts-card");
        
        Label alertsHeader = new Label("UNKNOWN PERSON LOGS");
        alertsHeader.getStyleClass().add("alerts-header");
        
        ScrollPane scroll = new ScrollPane();
        alertContainer = new VBox(15);
        scroll.setContent(alertContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        alertsCard.getChildren().addAll(alertsHeader, scroll);

        content.getChildren().addAll(feedCard, alertsCard);
        root.getChildren().addAll(header, content);
        
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/security.css").toExternalForm());

        startPolling();
    }

    private void handleManageFaces() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Registered Faces");
        dialog.setHeaderText("List of all registered people");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        List<String[]> faces = DatabaseManager.getAllRegisteredFaces();
        if (faces.isEmpty()) {
            content.getChildren().add(new Label("No faces registered yet."));
        } else {
            for (String[] face : faces) {
                HBox row = new HBox(15);
                row.getStyleClass().add("face-list-row");

                VBox info = new VBox(2);
                Label nameLbl = new Label(face[0]);
                nameLbl.getStyleClass().add("face-name");
                Label catLbl = new Label(face[1]);
                catLbl.getStyleClass().add("face-category");
                info.getChildren().addAll(nameLbl, catLbl);

                Region s = new Region();
                HBox.setHgrow(s, Priority.ALWAYS);

                Button delBtn = new Button("Delete");
                delBtn.getStyleClass().add("face-delete-btn");
                delBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + face[0] + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            // 1. Delete from database
                            DatabaseManager.deleteFace(face[0]);
                            
                            // 2. Reload embeddings
                            if (activeCamera != null) activeCamera.loadKnownFaces();
                            
                            // 3. Refresh dialog content (naive way: close and reopen or just remove row)
                            row.setVisible(false);
                            row.setManaged(false);
                            showAlert("Success", face[0] + " has been deleted.");
                        }
                    });
                });

                row.getChildren().addAll(info, s, delBtn);
                content.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.getStyleClass().add("face-list-scroll");

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/security.css").toExternalForm());
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void handleRegisterFace() {
        // Step 1: Select Category
        ChoiceDialog<String> categoryDialog = new ChoiceDialog<>("FAMILY", "CHILD", "FAMILY");
        categoryDialog.setTitle("Register Face");
        categoryDialog.setHeaderText("Category Selection");
        categoryDialog.setContentText("Select category for this person:");
        
        categoryDialog.showAndWait().ifPresent(category -> {
            String displayCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
            // Step 2: Select Source
            Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
            choice.setTitle("Register Face - " + displayCategory);
            choice.setHeaderText("Registration Source");
            choice.setContentText("Select source for the face photo:");

            ButtonType capBtn = new ButtonType("Capture Feed");
            ButtonType upBtn = new ButtonType("Upload File");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            choice.getButtonTypes().setAll(capBtn, upBtn, cancelBtn);

            choice.showAndWait().ifPresent(result -> {
                if (result == capBtn) {
                    registerFromCamera(displayCategory);
                } else if (result == upBtn) {
                    registerFromUpload(displayCategory);
                }
            });
        });
    }

    private void registerFromCamera(String category) {
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
        dialog.setHeaderText("New " + category + " Member");
        dialog.setContentText("Enter name for this person:");
        
        dialog.showAndWait().ifPresent(name -> {
            // Convert Mat to byte[] for database storage
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, mob);
            byte[] imageData = mob.toArray();
            
            DatabaseManager.registerFace(name, category, imageData);
            if (activeCamera != null) activeCamera.loadKnownFaces();
            showAlert("Success", name + " has been registered as " + category + "!");
        });
    }

    private void registerFromUpload(String category) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Face Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (selectedFile != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Register New Face");
            dialog.setHeaderText("Upload Successful");
            dialog.setContentText("Enter name for this " + category + " person:");

            dialog.showAndWait().ifPresent(name -> {
                try {
                    byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                    DatabaseManager.registerFace(name, category, imageData);
                    if (activeCamera != null) activeCamera.loadKnownFaces();
                    showAlert("Success", name + " has been registered as " + category + " from file!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to read file: " + e.getMessage());
                }
            });
        }
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
        List<Camera> cameras = devices.stream()
                .filter(d -> d instanceof Camera)
                .map(d -> (Camera) d)
                .toList();

        cameraSelector.getItems().clear();
        cameraSelector.getItems().addAll(cameras);

        if (!cameras.isEmpty()) {
            // CRITICAL: Set activeCamera BEFORE setValue to prevent race condition
            // where the event handler fires and sees a different camera
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

    private VBox createAlertCard(String time, String path, String msg) {
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
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this notification?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    Room selected = roomSelector.getValue();
                    if (selected != null) {
                        DatabaseManager.deleteAlert(selected.getId(), time, "SECURITY");
                        updateAlertsUI();
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
            int alertId = Integer.parseInt(path);
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

