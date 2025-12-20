package smartHome.javafx.Controllers;

import javafx.scene.control.Label;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;

public class LoginController {

    public void handleLogin(String username, String password, Label messageLabel) {

        try {
            if (DatabaseManager.validateLogin(username, password)) {
                messageLabel.setText("Login successful");
                SceneManager.switchScene("DashboardScene");
            } else {
                messageLabel.setText("Invalid username or password");
            }

        } catch (Exception e) {
            messageLabel.setText("Database error occurred");
            e.printStackTrace();
        }
    }
}
