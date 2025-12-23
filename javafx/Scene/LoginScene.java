package smartHome.javafx.Scene;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import smartHome.javafx.Controllers.LoginController;

public class LoginScene {

    public static Scene create() {

        Label title = new Label("Smart Home Login");
        title.getStyleClass().add("title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label messageLabel = new Label();

        Button loginButton = new Button("Login");
        Button signupButton = new Button("Sign Up");

        // Controller object
        LoginController controller = new LoginController();

        // ✅ Anonymous class + override (teacher requirement)
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.handleLogin(
                        usernameField.getText(),
                        passwordField.getText(),
                        messageLabel
                );
            }
        });

        // ✅ Lambda expression
        signupButton.setOnAction(e -> SceneManager.switchScene("Signup"));

        VBox root = new VBox(10,
                title,
                usernameField,
                passwordField,
                loginButton,

                signupButton,
                messageLabel
        );

        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(
                LoginScene.class.getResource("/smartHome/javafx/Css/login.css").toExternalForm()
        );

        return scene;
    }
}
