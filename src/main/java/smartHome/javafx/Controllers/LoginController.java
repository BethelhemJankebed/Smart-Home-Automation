package smartHome.app.controllers;

import javafx.scene.control.Label;
import smartHome.app.db.DatabaseManager;
import smartHome.app.utils.SceneManager;

public class LoginController {

    public void handleLogin(String username, String password, Label messageLabel) {

        try {
            if (DatabaseManager.validateLogin(username, password)) {
                messageLabel.setText("Login successful");
                SceneManager.switchToDashboard(); // future scene
            } else {
                messageLabel.setText("Invalid username or password");
            }

        } catch (Exception e) {
            messageLabel.setText("Database error occurred");
            e.printStackTrace();
        }
    }
}
