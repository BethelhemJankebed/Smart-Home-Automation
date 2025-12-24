package smartHome.javafx.Controllers;

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
        root.getStyleClass().add("root-appliances");

        // Header
        HBox header = new HBox(20);
        header.getStyleClass().add("header-container");
        
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> goBack());

        VBox titleBox = new VBox(2);
        Label title = new Label("My Smart Home");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Control and monitor your appliances by room");
        subtitle.getStyleClass().add("app-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addRoomBtn = new Button("+ Add Room");
        addRoomBtn.getStyleClass().add("add-room-button");
        addRoomBtn.setOnAction(e -> openAddRoom());

        header.getChildren().addAll(backBtn, titleBox, spacer, addRoomBtn);

        // Rooms Container
        roomsContainer = new FlowPane();
        roomsContainer.setHgap(25);
        roomsContainer.setVgap(25);
        
        ScrollPane scroll = new ScrollPane(roomsContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.getChildren().addAll(header, scroll);
        
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/appliances.css").toExternalForm());
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
        card.getStyleClass().add("admin-report-card");
        
        Label iconLabel = new Label("📊");
        iconLabel.getStyleClass().add("report-icon");
        
        Label nameLabel = new Label("Admin Reports");
        nameLabel.getStyleClass().add("report-title");
        
        Label desc = new Label("View system logs");
        desc.getStyleClass().add("report-desc");

        card.getChildren().addAll(iconLabel, nameLabel, desc);
        card.setOnMouseClicked(e -> SceneManager.switchScene("Reports"));
        roomsContainer.getChildren().add(card);
    }

    private void addRoomToUI(Room room) {
        String name = room.getName();
        String icon = room.getIcon();

        VBox card = new VBox(12);
        card.setPrefWidth(240);
        card.getStyleClass().add("room-card");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        String colorClass = switch(icon != null ? icon : "") {
            case "🍳" -> "color-orange"; 
            case "🛏" -> "color-indigo"; 
            case "🚿" -> "color-cyan"; 
            case "🧒" -> "color-pink"; 
            case "📺" -> "color-violet"; 
            case "🧺" -> "color-yellow"; 
            case "🚗" -> "color-red"; 
            case "🌳" -> "color-green"; 
            default -> "color-blue";
        };

        StackPane iconPane = new StackPane();
        Circle circle = new Circle(26);
        circle.getStyleClass().addAll("icon-bg", colorClass); // Tinted background due to opacity
        
        Label iconLabel = new Label(icon != null ? icon : "🏠");
        iconLabel.getStyleClass().addAll("icon-label", colorClass);
        iconPane.getChildren().addAll(circle, iconLabel);
        
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        
        Button removeBtn = new Button("×");
        removeBtn.getStyleClass().add("remove-button");
        removeBtn.setOnAction(e -> {
            // DatabaseManager.deleteRoom(room.getId()); // Optional: implement deletion
            roomsContainer.getChildren().remove(card);
        });
        
        topRow.getChildren().addAll(iconPane, s, removeBtn);

        VBox infoBox = new VBox(4);
        Label roomName = new Label(name);
        roomName.getStyleClass().add("room-title");
        
        int active = DatabaseManager.getActiveDeviceCountForRoom(room.getId());
        int total = DatabaseManager.getTotalDeviceCountForRoom(room.getId());
        
        Label statusLabel = new Label(active + " / " + total + " Devices ON");
        if (active > 0) {
            statusLabel.getStyleClass().add("status-on");
        } else {
            statusLabel.getStyleClass().add("status-off");
        }
        
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
