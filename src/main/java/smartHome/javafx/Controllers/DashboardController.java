package smartHome.javafx.Controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import smartHome.javafx.Scene.SceneManager;

public class DashboardController {

    @FXML
    private void handleControlHome() {
        SceneManager.switchScene("AppliancesModule");
    }

    @FXML
    private void handleLogout() {
        SceneManager.switchScene("Login");
    }
}
