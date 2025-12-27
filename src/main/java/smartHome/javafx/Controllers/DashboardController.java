package smartHome.javafx.Controllers;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import smartHome.javafx.Scene.SceneManager;

public class DashboardController {

    public void bindUI(Button logoutBtn, VBox childModule, VBox homeModule, VBox securityModule, VBox reportModule) {

        // --- Logout ---
        logoutBtn.setOnAction(e -> {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Logout Confirmation");
            confirm.setHeaderText(null);
            
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            javafx.scene.control.Label title = new javafx.scene.control.Label("Confirm Logout");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
            javafx.scene.control.Label msg = new javafx.scene.control.Label("Are you sure you want to logout?");
            msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            content.getChildren().addAll(title, msg);
            
            javafx.scene.control.Label icon = new javafx.scene.control.Label("🚪");
            icon.setStyle("-fx-font-size: 36px; -fx-text-fill: #3b82f6;");
            
            confirm.getDialogPane().setContent(content);
            confirm.setGraphic(icon);
            confirm.getDialogPane().getStylesheets().add(getClass().getResource("/smartHome/javafx/Css/dialog.css").toExternalForm());
            confirm.getDialogPane().getStyleClass().add("dialog-pane");
            confirm.getButtonTypes().setAll(javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);

            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.YES) {
                    SceneManager.switchScene("Login");
                }
            });
        });

        // --- Modules ---
        childModule.setOnMouseClicked(e -> SceneManager.switchScene("MonitorChild"));
        homeModule.setOnMouseClicked(e -> SceneManager.switchScene("AppliancesModule"));
        securityModule.setOnMouseClicked(e -> SceneManager.switchScene("SecurityModule"));
        reportModule.setOnMouseClicked(e -> SceneManager.switchScene("Reports"));
    }
}
