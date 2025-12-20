package smartHome.javafx.Scene;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import smartHome.javafx.Controllers.ChildMonitorController;

public class ChildMonitorScene {
    public static Scene create() {

        ChildMonitorController controller = new ChildMonitorController();

        // Header
        Label title = new Label("Monitor Child");
        title.getStyleClass().add("cm-title");
        Button back = new Button("Back");
        back.getStyleClass().add("secondary-button");
        back.setOnAction(e -> SceneManager.switchScene("Dashboard"));
        BorderPane header = new BorderPane(title, null, back, null, null);

        // Live panel
        Label liveDot = new Label();
        liveDot.setGraphic(new Circle(6, Color.RED));
        Label liveText = new Label("Live");
        HBox liveHeader = new HBox(8, liveDot, liveText);
        liveHeader.setAlignment(Pos.CENTER_LEFT);
        Pane liveView = new Pane();
        liveView.setMinHeight(320);
        liveView.getStyleClass().add("live-view");
        VBox livePanel = new VBox(8, liveHeader, liveView);
        livePanel.getStyleClass().add("panel");
        VBox.setVgrow(liveView, Priority.ALWAYS);

        // Notifications
        Label notifText = new Label("Notifications");
        notifText.getStyleClass().add("panel-title");
        Label notifBadge = new Label();
        notifBadge.getStyleClass().add("badge-red");
        notifBadge.textProperty().bind(controller.notificationCountProperty().asString());
        var badgeVisible = Bindings.createBooleanBinding(
                () -> controller.notificationCountProperty().get() > 0,
                controller.notificationCountProperty()
        );
        notifBadge.visibleProperty().bind(badgeVisible);
        notifBadge.managedProperty().bind(badgeVisible);

        HBox notifHeader = new HBox(8, notifText, notifBadge);
        notifHeader.setAlignment(Pos.CENTER_LEFT);
        VBox notifPanel = new VBox(8, notifHeader);
        notifPanel.getStyleClass().add("panel");

        notifPanel.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("Recent Alert");
            alert.setContentText("Child monitor alert");
            alert.getDialogPane().getStylesheets().add(ChildMonitorScene.class.getResource("/css/childmonitor.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("confirm-dialog");
            Label imgBox = new Label("img");
            imgBox.getStyleClass().add("dialog-img");
            Label text = new Label("Warning text");
            VBox graphic = new VBox(8, imgBox, text);
            alert.setGraphic(graphic);
            alert.showAndWait();
            controller.decrementNotifications();
        });

        VBox rightCol = new VBox(12, notifPanel);
        rightCol.setPrefWidth(260);

        HBox content = new HBox(16, livePanel, rightCol);
        HBox.setHgrow(livePanel, Priority.ALWAYS);

        VBox root = new VBox(16, header, content);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("child-root");

        Scene scene = new Scene(root, 1100, 650);
        scene.getStylesheets().add(ChildMonitorScene.class.getResource("/css/childmonitor.css").toExternalForm());
        scene.getStylesheets().add(ChildMonitorScene.class.getResource("/css/dashboard.css").toExternalForm());
        return scene;
    }
}