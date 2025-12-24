package smartHome.javafx.Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import smartHome.javafx.Scene.SceneManager;
import smartHome.db.DatabaseManager;
import smartHome.app.Room;
import java.util.List;

public class ControlHomeAppliancesController {

    private VBox root;
    private FlowPane roomsContainer;

    public ControlHomeAppliancesController() {
        // Initialize UI with a premium background
        root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #f1f5f9);");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 18px; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setOnAction(e -> goBack());

        VBox titleBox = new VBox(2);
        Label title = new Label("My Smart Home");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Control and monitor your appliances by room");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addRoomBtn = new Button("+ Add Room");
        addRoomBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        addRoomBtn.setOnAction(e -> openAddRoom());

        header.getChildren().addAll(backBtn, titleBox, spacer, addRoomBtn);

        // Rooms Container
        roomsContainer = new FlowPane();
        roomsContainer.setHgap(25);
        roomsContainer.setVgap(25);
        
        ScrollPane scroll = new ScrollPane(roomsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.getChildren().addAll(header, scroll);
    }
    
    public VBox getView() {
        return root;
    }

    public void initialize() {
        roomsContainer.getChildren().clear();
        // Load rooms from DB
        List<Room> rooms = DatabaseManager.getAllRooms();
        for (Room r : rooms) {
            addRoomToUI(r); 
        }
        addReportsCard();
    }

    private void addReportsCard() {
        VBox card = new VBox(15);
        card.setPrefWidth(240);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #eff6ff, #dbeafe); -fx-background-radius: 20; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.2), 20, 0, 0, 10); -fx-cursor: hand;");
        
        Label iconLabel = new Label("📊");
        iconLabel.setStyle("-fx-font-size: 44px;");
        
        Label nameLabel = new Label("Admin Reports");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #1e40af;");
        
        Label desc = new Label("View system logs");
        desc.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 11px;");

        card.getChildren().addAll(iconLabel, nameLabel, desc);
        card.setOnMouseClicked(e -> SceneManager.switchScene("Reports"));
        roomsContainer.getChildren().add(card);
    }

    private void addRoomToUI(Room room) {
        String name = room.getName();
        String icon = room.getIcon();

        VBox card = new VBox(12);
        card.setPrefWidth(240);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 8); -fx-cursor: hand;");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconPane = new StackPane();
        Circle circle = new Circle(26);
        circle.setStyle("-fx-fill: #f1f5f9;");
        Label iconLabel = new Label(icon != null ? icon : "🏠");
        iconLabel.setStyle("-fx-font-size: 26px;");
        iconPane.getChildren().addAll(circle, iconLabel);
        
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        
        Button removeBtn = new Button("×");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 20px; -fx-font-weight: bold;");
        removeBtn.setOnAction(e -> {
            // DatabaseManager.deleteRoom(room.getId()); // Optional: implement deletion
            roomsContainer.getChildren().remove(card);
        });
        
        topRow.getChildren().addAll(iconPane, s, removeBtn);

        VBox infoBox = new VBox(4);
        Label roomName = new Label(name);
        roomName.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        int active = DatabaseManager.getActiveDeviceCountForRoom(room.getId());
        int total = DatabaseManager.getTotalDeviceCountForRoom(room.getId());
        
        Label statusLabel = new Label(active + " / " + total + " Devices ON");
        statusLabel.setStyle("-fx-text-fill: " + (active > 0 ? "#10b981" : "#94a3b8") + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        infoBox.getChildren().addAll(roomName, statusLabel);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() != removeBtn) {
                SceneManager.switchScene("ControlRoomDevice", room);
            }
        });

        card.getChildren().addAll(topRow, infoBox);
        roomsContainer.getChildren().add(card);
    }

    private void openAddRoom() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Add New Room");
        nameDialog.setHeaderText("Create a new room for your home");
        nameDialog.setContentText("Room Name:");

        nameDialog.showAndWait().ifPresent(name -> {
            ChoiceDialog<String> iconDialog = new ChoiceDialog<>("🏠",
                    "🏠", "🍳", "🛏", "🚿", "🧒", "📺", "🧺", "🚗", "🌳");

            iconDialog.setTitle("Select Icon");
            iconDialog.setHeaderText("Pick an icon that represents this room");
            iconDialog.setContentText("Icon:");

            iconDialog.showAndWait().ifPresent(icon -> {
                int id = DatabaseManager.insertRoom(name, icon);
                if (id != -1) {
                    addRoomToUI(new Room(id, name, icon));
                }
            });
        });
    }

    private void goBack() {
        SceneManager.switchScene("Dashboard");
    }
}
