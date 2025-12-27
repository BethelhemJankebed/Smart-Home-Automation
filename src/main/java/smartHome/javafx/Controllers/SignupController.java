package smartHome.javafx.Controllers;

import javafx.scene.control.Label;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;

public class SignupController {

    public void handleSignup(String username, String password, Label errorLabel) {

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("All fields are required");
            return;
        }

        if (DatabaseManager.registerUser(username, password)) {
            SceneManager.switchScene("Login");
        } else {
            errorLabel.setText("Username already exists");
        }
    }
}
