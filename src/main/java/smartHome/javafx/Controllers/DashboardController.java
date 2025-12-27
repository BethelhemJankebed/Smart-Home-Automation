package smartHome.javafx.Controllers;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import smartHome.javafx.Scene.SceneManager;

public class DashboardController {

    public void bindUI(Button logoutBtn, VBox childModule, VBox homeModule, VBox securityModule, VBox reportModule) {

        // --- Logout ---
        logoutBtn.setOnAction(e -> SceneManager.switchScene("Login"));

        // --- Modules ---
        childModule.setOnMouseClicked(e -> SceneManager.switchScene("MonitorChild"));
        homeModule.setOnMouseClicked(e -> SceneManager.switchScene("AppliancesModule"));
        securityModule.setOnMouseClicked(e -> SceneManager.switchScene("SecurityModule"));
        reportModule.setOnMouseClicked(e -> SceneManager.switchScene("Reports"));
    }
}
