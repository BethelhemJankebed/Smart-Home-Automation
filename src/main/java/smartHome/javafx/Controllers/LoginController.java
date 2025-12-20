package smartHome.javafx.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import smartHome.db.DatabaseManager;
import smartHome.javafx.Scene.SceneManager;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (DatabaseManager.validateLogin(username, password)) {
            SceneManager.switchScene("Dashboard");
        } else {
            errorLabel.setText("Invalid username or password");
        }
    }

    @FXML
    private void goToSignup() {
        SceneManager.switchScene("Signup");
    }
}
