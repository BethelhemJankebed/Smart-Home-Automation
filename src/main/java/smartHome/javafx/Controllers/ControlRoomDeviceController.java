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
            DatabaseManager.logEvent(d.getId(), newState ? "Turned ON" : "Turned OFF", "");
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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + d.getName() + "?", ButtonType.YES, ButtonType.NO);
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
            Alert a = new Alert(Alert.AlertType.WARNING, "No cameras found in this room to link.");
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
                // ChoiceDialog if multiple
                ChoiceDialog<Camera> dialog = new ChoiceDialog<>(cameras.get(0), cameras);
                dialog.setTitle("Select Camera");
                dialog.setHeaderText("Link " + light.getName() + " to which camera?");
                dialog.setContentText("Camera:");
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
        dialog.setHeaderText("Add a new device to " + currentRoom.getName());

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

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

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(sourceLabel, 0, 2);
        grid.add(sourceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText();
                String type = typeCombo.getValue();
                String id = "D" + System.currentTimeMillis(); // Simple ID generation
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
        TextInputDialog dialog = new TextInputDialog(cam.getStreamSource());
        dialog.setTitle("Camera Settings");
        dialog.setHeaderText("Edit stream source for: " + cam.getName());
        dialog.setContentText("Enter Camera Index (0, 1...) or Stream URL:");

        dialog.showAndWait().ifPresent(newSource -> {
            cam.setStreamSource(newSource);
            DatabaseManager.addDevice(cam, currentRoom.getId());
            loadDevices();
            DatabaseManager.logEvent(cam.getId(), "Source Updated", "New source: " + newSource);
            refreshLogs();
        });
    }
}
