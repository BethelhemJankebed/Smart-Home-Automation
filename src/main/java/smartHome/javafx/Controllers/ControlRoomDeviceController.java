package smartHome.javafx.Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import smartHome.app.Device;
import smartHome.app.Room;
import smartHome.app.Light;
import smartHome.app.Fan;
import smartHome.app.DoorLock;
import smartHome.app.TV;
import smartHome.app.Camera;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;
import java.util.List;

public class ControlRoomDeviceController {

    private VBox root;
    private FlowPane devicesContainer;
    private VBox logContainer;
    private Room currentRoom;

    public ControlRoomDeviceController() {
        HBox mainLayout = new HBox(40);
        mainLayout.getStyleClass().add("root-room");
        
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // LEFT SIDE: Devices
        VBox leftSide = new VBox(25);
        HBox.setHgrow(leftSide, Priority.ALWAYS);
        
        // 1. Header
        HBox header = new HBox(20);
        header.getStyleClass().add("header-container");
        
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> SceneManager.switchScene("AppliancesModule")); 

        VBox titleBox = new VBox(2);
        Label title = new Label("Room Devices");
        title.getStyleClass().add("room-title");
        Label subtitle = new Label("Manage your room appliances");
        subtitle.getStyleClass().add("room-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        
        Button addDeviceBtn = new Button("+ Add New Device");
        addDeviceBtn.getStyleClass().add("add-device-button");
        addDeviceBtn.setOnAction(e -> openAddDeviceDialog());

        header.getChildren().addAll(backBtn, titleBox, s1, addDeviceBtn);

        // 2. Devices Container
        devicesContainer = new FlowPane();
        devicesContainer.setHgap(20);
        devicesContainer.setVgap(20);
        
        ScrollPane scroll = new ScrollPane(devicesContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        leftSide.getChildren().addAll(header, scroll);

        // RIGHT SIDE: Activity Feed
        VBox rightSide = new VBox(20);
        rightSide.setPrefWidth(320);
        rightSide.setMinWidth(320);
        rightSide.getStyleClass().add("activity-feed-container");

        Label logTitle = new Label("Activity Feed");
        logTitle.getStyleClass().add("activity-title");
        
        logContainer = new VBox(12);
        ScrollPane logScroll = new ScrollPane(logContainer);
        logScroll.setFitToWidth(true);
        logScroll.getStyleClass().add("transparent-scroll-pane");
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(logScroll, Priority.ALWAYS);

        rightSide.getChildren().addAll(logTitle, logScroll);

        mainLayout.getChildren().addAll(leftSide, rightSide);

        root = new VBox(mainLayout); 
        root.setFillWidth(true);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/room-device.css").toExternalForm());

        // Load Data
        Object data = SceneManager.getData();
        if (data instanceof Room r) {
            currentRoom = r;
            title.setText(r.getName());
            subtitle.setText(r.getIcon() + " Smart " + r.getName());
            loadDevices();
            refreshLogs();
        }
    }
    
    // Updated to accept style classes instead of raw hex
    private VBox createLogEntry(String action, String deviceName, String time, String bgClass, String textClass) {
        VBox entry = new VBox(4);
        entry.getStyleClass().addAll("log-entry-base", bgClass);
        
        Label actionLbl = new Label(action.toUpperCase());
        actionLbl.getStyleClass().addAll("log-action-text", textClass);
        
        Label deviceLbl = new Label(deviceName);
        deviceLbl.getStyleClass().add("log-device-name");
        
        Label timeLbl = new Label("at " + time);
        timeLbl.getStyleClass().addAll("log-time-text", textClass);
        
        entry.getChildren().addAll(actionLbl, deviceLbl, timeLbl);
        return entry;
    }

    public VBox getView() {
        return root;
    }

    private void loadDevices() {
        devicesContainer.getChildren().clear();
        if (currentRoom == null) return;

        List<Device> devices = DatabaseManager.getDevicesForRoom(currentRoom.getId());
        
        // HYDRATE ROOM: Populate the room's device list so Cameras can find Lights
        currentRoom.getDevices().clear();
        for (Device d : devices) {
            currentRoom.addDevice(d);
            if (d instanceof Camera cam) {
                cam.setLinkedRoom(currentRoom);
            }
        }

        for (Device d : devices) {
            devicesContainer.getChildren().add(createDeviceCard(d));
        }
    }

    private void refreshLogs() {
        if (logContainer == null || currentRoom == null) return;
        logContainer.getChildren().clear();
        
        List<String[]> events = DatabaseManager.getEventsForRoom(currentRoom.getId());
        for (String[] ev : events) {
            // Determine styling classes based on event content
            String bgClass = "log-bg-green";
            String textClass = "log-text-green";
            
            if (ev[0].contains("OFF")) {
                bgClass = "log-bg-gray";
                textClass = "log-text-gray";
            } else if (ev[0].contains("Motion")) {
                bgClass = "log-bg-red";
                textClass = "log-text-red";
            }
            logContainer.getChildren().add(createLogEntry(ev[0], ev[1], ev[2], bgClass, textClass));
        }
    }

    private VBox createDeviceCard(Device d) {
        VBox card = new VBox(15);
        card.setPrefWidth(160);
        card.getStyleClass().add("device-card");

        String icon = switch(d.getType()) {
            case "Light" -> "💡";
            case "Fan" -> "🌀";
            case "TV" -> "📺";
            case "DoorLock" -> "🔒";
            case "Camera" -> "📹";
            default -> "⚙️";
        };

        String colorClass = switch(d.getType()) {
            case "Light" -> "color-amber"; 
            case "Fan" -> "color-blue";
            case "TV" -> "color-violet";
            case "DoorLock" -> "color-teal";
            case "Camera" -> "color-rose";
            default -> "color-slate";
        };

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().addAll("device-icon", colorClass);

        VBox titleBox = new VBox(2);
        titleBox.setAlignment(Pos.CENTER);
        Label nameInfo = new Label(d.getName());
        nameInfo.getStyleClass().add("device-name");
        
        Label typeInfo = new Label(d.getType());
        typeInfo.getStyleClass().add("device-type");
        titleBox.getChildren().addAll(nameInfo, typeInfo);

        ToggleButton powerBtn = new ToggleButton(d.isOn() ? "ENABLED" : "DISABLED");
        powerBtn.setSelected(d.isOn());
        powerBtn.setPrefWidth(120);
        
        // Initial state style
        powerBtn.getStyleClass().add("toggle-btn-base");
        powerBtn.getStyleClass().add(d.isOn() ? "toggle-on" : "toggle-off");
        
        // Listener for external updates (e.g. from Camera)
        d.setOnStateChanged(() -> javafx.application.Platform.runLater(() -> {
            boolean on = d.isOn();
            powerBtn.setSelected(on);
            powerBtn.setText(on ? "ENABLED" : "DISABLED");
            updateToggleStyle(powerBtn, on);
        }));
        
        powerBtn.setOnAction(e -> {
            boolean newState = powerBtn.isSelected();
            powerBtn.setText(newState ? "ENABLED" : "DISABLED");
            updateToggleStyle(powerBtn, newState);
            
            if (newState) d.turnOn(); else d.turnOff();
            
            // Update state in DB
            DatabaseManager.addDevice(d, currentRoom.getId());
            
            // Log Event
            DatabaseManager.logEvent(d.getId(), newState ? "Turned ON" : "Turned OFF", d.getType() + " | " + d.getName());
            refreshLogs();
        });

        Button settingsBtn = null;
        if (d instanceof Camera cam) {
            settingsBtn = new Button("⚙ Settings");
            settingsBtn.getStyleClass().add("settings-btn");
            settingsBtn.setOnAction(e -> openEditCameraSourceDialog(cam));
        }

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Device");
            alert.setHeaderText(null);
            
            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label("Confirm Deletion");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
            Label msg = new Label("Are you sure you want to delete " + d.getName() + "?");
            msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            content.getChildren().addAll(title, msg);
            
            Label deleteIcon = new Label("🗑");
            deleteIcon.setStyle("-fx-font-size: 36px; -fx-text-fill: #ef4444;");
            
            alert.getDialogPane().setContent(content);
            alert.setGraphic(deleteIcon);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dialog.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    DatabaseManager.deleteDevice(d.getId());
                    loadDevices(); // Refresh list
                    DatabaseManager.logEvent("SYS", "Device Deleted", d.getName() + " removed from " + currentRoom.getName());
                    refreshLogs();
                }
            });
        });

        HBox topRow = new HBox(deleteBtn);
        topRow.setAlignment(Pos.CENTER_RIGHT);

        if (settingsBtn != null) {
            card.getChildren().addAll(topRow, iconLabel, titleBox, powerBtn, settingsBtn);
        } else {
            // Check if it's a light and we have a camera in the room to link to
            if (d instanceof Light light) {
                 Button linkBtn = new Button(light.isLinked() ? "🔗 Linked" : "🔗 Link");
                 updateLinkButtonStyle(linkBtn, light.isLinked());
                 
                 linkBtn.setOnAction(e -> {
                     handleLinkLight(light, linkBtn);
                 });
                 
                 card.getChildren().addAll(topRow, iconLabel, titleBox, powerBtn, linkBtn);
            } else {
                 card.getChildren().addAll(topRow, iconLabel, titleBox, powerBtn);
            }
        }
        return card;
    }
    
    private void updateToggleStyle(ToggleButton btn, boolean isOn) {
        btn.getStyleClass().removeAll("toggle-on", "toggle-off");
        btn.getStyleClass().add(isOn ? "toggle-on" : "toggle-off");
    }

    private void updateLinkButtonStyle(Button btn, boolean isLinked) {
        btn.getStyleClass().removeAll("link-btn-on", "link-btn-off");
        btn.getStyleClass().add(isLinked ? "link-btn-on" : "link-btn-off");
    }
    
    private void handleLinkLight(Light light, Button btn) {
        // Find cameras in this room
        List<Camera> cameras = new java.util.ArrayList<>();
        if (currentRoom != null) {
            for (Device d : currentRoom.getDevices()) {
                if (d instanceof Camera c) cameras.add(c);
            }
        }
        
        if (cameras.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("No Cameras");
            a.setHeaderText(null);
            
            VBox c = new VBox(10);
            c.setAlignment(Pos.CENTER_LEFT);
            Label t = new Label("Action Unavailable");
            t.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
            Label m = new Label("No cameras found in this room to link.");
            m.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            c.getChildren().addAll(t, m);
            
            Label ic = new Label("⚠️");
            ic.setStyle("-fx-font-size: 36px; -fx-text-fill: #f59e0b;");
            
            a.getDialogPane().setContent(c);
            a.setGraphic(ic);
            a.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dialog.css").toExternalForm());
            a.getDialogPane().getStyleClass().add("dialog-pane");
            
            a.show();
            return;
        }
        
        if (light.isLinked()) {
            // Unlink
            light.unlinkCamera();
            DatabaseManager.addDevice(light, currentRoom.getId());
            btn.setText("🔗 Link");
            updateLinkButtonStyle(btn, false);
            DatabaseManager.logEvent(light.getId(), "LINK", "Unlinked from camera");
        } else {
            // Link
            Camera target = cameras.get(0); // Default to first
            if (cameras.size() > 1) {
                // ChoiceDialog replacement
                Dialog<Camera> dialog = new Dialog<>();
                dialog.setTitle("Select Camera");
                dialog.setHeaderText(null);
                
                VBox camContent = new VBox(10);
                camContent.setAlignment(Pos.CENTER_LEFT);
                
                Label cTitle = new Label("Link Camera");
                cTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
                Label cMsg = new Label("Select a camera to link to " + light.getName());
                cMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
                
                ComboBox<Camera> camCombo = new ComboBox<>();
                camCombo.getItems().addAll(cameras);
                camCombo.setValue(cameras.get(0));
                camCombo.setMaxWidth(Double.MAX_VALUE);
                
                // Converter for ComboBox display
                camCombo.setConverter(new javafx.util.StringConverter<Camera>() {
                    @Override public String toString(Camera c) { return c != null ? c.getName() : ""; }
                    @Override public Camera fromString(String s) { return null; }
                });
                
                camContent.getChildren().addAll(cTitle, cMsg, camCombo);
                
                Label camIcon = new Label("📹");
                camIcon.setStyle("-fx-font-size: 36px; -fx-text-fill: #3b82f6;");
                
                dialog.getDialogPane().setContent(camContent);
                dialog.setGraphic(camIcon);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                
                dialog.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dialog.css").toExternalForm());
                dialog.getDialogPane().getStyleClass().add("dialog-pane");
                
                dialog.setResultConverter(b -> b == ButtonType.OK ? camCombo.getValue() : null);

                java.util.Optional<Camera> result = dialog.showAndWait();
                if (result.isPresent()) target = result.get(); else return;
            }
            
            light.setLinkedCameraId(target.getId());
            DatabaseManager.addDevice(light, currentRoom.getId());
            btn.setText("🔗 Linked");
            updateLinkButtonStyle(btn, true);
            DatabaseManager.logEvent(light.getId(), "LINK", "Linked to camera " + target.getName());
        }
        refreshLogs();
    }

    private void openAddDeviceDialog() {
        if (currentRoom == null) return;

        Dialog<Device> dialog = new Dialog<>();
        dialog.setTitle("Add Device");
        dialog.setHeaderText(null);

        // Header Content
        VBox mainBox = new VBox(20);
        
        VBox headerContent = new VBox(5);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Add New Device");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Add a new device to " + currentRoom.getName());
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        headerContent.getChildren().addAll(title, subtitle);

        // Form Content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        TextField nameField = new TextField();
        nameField.setPromptText("Device Name");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Light", "Fan", "TV", "DoorLock", "Camera");
        typeCombo.setValue("Light");

        TextField sourceField = new TextField("0");
        sourceField.setPromptText("Cam Index (0, 1) or Stream URL");
        Label sourceLabel = new Label("Stream Source:");
        
        sourceLabel.setVisible(false);
        sourceField.setVisible(false);

        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCam = "Camera".equals(newVal);
            sourceLabel.setVisible(isCam);
            sourceField.setVisible(isCam);
        });

        // Styling labels for form
        Label l1 = new Label("Name:"); l1.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        Label l2 = new Label("Type:"); l2.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        Label l3 = new Label("Source:"); l3.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        l3.visibleProperty().bind(sourceLabel.visibleProperty()); // Bind visibility

        grid.add(l1, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(l2, 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(l3, 0, 2);
        grid.add(sourceField, 1, 2);
        
        mainBox.getChildren().addAll(headerContent, grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(mainBox);
        
        // Icon
        Label icon = new Label("✨");
        icon.setStyle("-fx-font-size: 36px; -fx-text-fill: #f59e0b;");
        dialog.setGraphic(icon);

        // Styling
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dialog.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText();
                String type = typeCombo.getValue();
                String id = "D" + System.currentTimeMillis();
                switch(type) {
                    case "Light": return new Light(id, name);
                    case "Fan": return new Fan(id, name);
                    case "TV": return new TV(id, name);
                    case "DoorLock": return new DoorLock(id, name);
                    case "Camera": {
                        Camera c = new Camera(id, name, currentRoom);
                        c.setStreamSource(sourceField.getText());
                        return c;
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(device -> {
            if (device != null) {
                DatabaseManager.addDevice(device, currentRoom.getId());
                loadDevices();
            }
        });
    }

    private void openEditCameraSourceDialog(Camera cam) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Camera Settings");
        dialog.setHeaderText(null);
        
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Edit Stream Source");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label msg = new Label("Enter Camera Index (0, 1...) or Stream URL for: " + cam.getName());
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        TextField input = new TextField(cam.getStreamSource());
        
        content.getChildren().addAll(title, msg, input);
        
        Label icon = new Label("⚙");
        icon.setStyle("-fx-font-size: 36px; -fx-text-fill: #64748b;");
        
        dialog.getDialogPane().setContent(content);
        dialog.setGraphic(icon);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dialog.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        dialog.setResultConverter(b -> b == ButtonType.OK ? input.getText() : null);

        dialog.showAndWait().ifPresent(newSource -> {
            cam.setStreamSource(newSource);
            DatabaseManager.addDevice(cam, currentRoom.getId());
            loadDevices();
            DatabaseManager.logEvent(cam.getId(), "Source Updated", "New source: " + newSource);
            refreshLogs();
        });
    }
}
