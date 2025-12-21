package smartHome.javafx.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();

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

    @FXML
    private void goBack() {
        SceneManager.switchScene("Login");
    }
}
