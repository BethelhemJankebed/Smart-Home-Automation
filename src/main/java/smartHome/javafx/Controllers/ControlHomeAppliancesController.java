package smartHome.app.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import smartHome.app.utils.SceneManager;

public class ControlHomeAppliancesController {

    @FXML
    private FlowPane roomsContainer;

    @FXML
    public void initialize() {
        // Sample rooms
        addRoom("Living Room", "🏠");
        addRoom("Kitchen", "🍳");
        addRoom("Bedroom", "🛏");
    }

    private void addRoom(String name, String icon) {

        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 18, 0, 0, 6);
        """);

        StackPane iconPane = new StackPane();
        Circle circle = new Circle(24);
        circle.setStyle("-fx-fill: #00c94f;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
        iconPane.getChildren().addAll(circle, iconLabel);

        Label roomName = new Label(name);
        roomName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label devices = new Label("Devices available");
        devices.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #dc2626;
        """);
        removeBtn.setOnAction(e -> roomsContainer.getChildren().remove(card));

        card.getChildren().addAll(iconPane, roomName, devices, removeBtn);
        roomsContainer.getChildren().add(card);
    }

    @FXML
    private void openAddRoom() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setHeaderText("Add Room");
        nameDialog.setContentText("Room name:");

        nameDialog.showAndWait().ifPresent(name -> {

            ChoiceDialog<String> iconDialog = new ChoiceDialog<>("🏠",
                    "🏠", "🍳", "🛏", "🚿", "🧒", "📺");

            iconDialog.setHeaderText("Choose Room Icon");
            iconDialog.setContentText("Icon:");

            iconDialog.showAndWait().ifPresent(icon ->
                    addRoom(name, icon)
            );
        });
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("Dashboard");
    }
}
